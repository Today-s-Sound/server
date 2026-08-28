package com.todaysound.todaysound_server.global.application;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.EXTERNAL_API;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import com.todaysound.todaysound_server.global.utils.CryptoUtils;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private static final int MULTICAST_LIMIT = 500;
    private static final String CLIENT_RESPONSE_MISMATCH = "CLIENT_RESPONSE_MISMATCH";
    private static final String CLIENT_RUNTIME_EXCEPTION = "CLIENT_RUNTIME_EXCEPTION";
    private static final Set<MessagingErrorCode> RETRYABLE_MESSAGING_ERRORS = EnumSet.of(
            MessagingErrorCode.INTERNAL,
            MessagingErrorCode.UNAVAILABLE,
            MessagingErrorCode.QUOTA_EXCEEDED
    );
    private static final Set<ErrorCode> RETRYABLE_FIREBASE_ERRORS = EnumSet.of(
            ErrorCode.ABORTED,
            ErrorCode.CANCELLED,
            ErrorCode.DATA_LOSS,
            ErrorCode.DEADLINE_EXCEEDED,
            ErrorCode.INTERNAL,
            ErrorCode.RESOURCE_EXHAUSTED,
            ErrorCode.UNAVAILABLE,
            ErrorCode.UNKNOWN
    );

    private final FCMRepository fcmRepository;
    private final HeaderAuthValidator headerAuthValidator;
    private final FirebaseMessagingClient firebaseMessagingClient;
    private final FcmTokenLifecycleService fcmTokenLifecycleService;

    /**
     * (핵심 메소드) 특정 User에게 알림을 발송합니다.
     *
     * @param user  알림을 받을 User 엔티티
     * @param title 알림 제목
     * @param body  알림 본문
     */
    public void sendNotificationToUser(User user, String title, String body) {
        List<FCM_Token> devices = fcmRepository.findByUserAndIsActiveTrue(user);

        if (devices.isEmpty()) {
            log.warn(EXTERNAL_API, "알림을 보낼 기기 토큰 없음 {}", kv("userId", user.getId()));
            return;
        }

        String eventId = CryptoUtils.sha256("manual:" + UUID.randomUUID());
        List<FcmTarget> unregisteredTokens = new ArrayList<>();

        for (int start = 0; start < devices.size(); start += MULTICAST_LIMIT) {
            int end = Math.min(start + MULTICAST_LIMIT, devices.size());
            List<FcmTarget> targets = devices.subList(start, end).stream()
                    .map(device -> new FcmTarget(device.getId(), device.getFcmToken()))
                    .toList();

            sendMulticast(title, body, eventId, targets).stream()
                    .filter(FcmSendResult::unregistered)
                    .map(result -> new FcmTarget(result.referenceId(), result.attemptedToken()))
                    .forEach(unregisteredTokens::add);
        }

        fcmTokenLifecycleService.deactivateAllIfTokenMatches(unregisteredTokens);
    }

    public List<FcmSendResult> sendMulticast(
            String title,
            String body,
            String eventId,
            List<FcmTarget> targets
    ) {
        if (targets.isEmpty()) {
            return List.of();
        }
        if (targets.size() > MULTICAST_LIMIT) {
            throw new IllegalArgumentException("FCM multicast supports at most 500 targets");
        }
        if (eventId == null || !eventId.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("eventId must be a 64-character SHA-256 hex value");
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .putHeader("apns-priority", "10")
                    // APNs에 대기 중인 동일 이벤트의 병합을 요청하며 이미 표시된 알림까지 제거하지는 않는다.
                    .putHeader("apns-collapse-id", eventId)
                    .setAps(Aps.builder().setSound("default").setBadge(1).build())
                    .build();

            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(notification)
                    .setApnsConfig(apnsConfig)
                    // 클라이언트가 eventId를 기준으로 중복 표시를 방지할 수 있도록 함께 전달한다.
                    .putData("eventId", eventId)
                    .addAllTokens(targets.stream().map(FcmTarget::token).toList())
                    .build();

            BatchResponse response = firebaseMessagingClient.sendEachForMulticast(message);
            List<FcmSendResult> results = mapResponse(response, targets);
            logResultSummary(results);
            return results;
        } catch (FirebaseMessagingException exception) {
            log.error(EXTERNAL_API, "FCM Multicast 발송 실패 {} {}",
                    kv("errorCode", errorCodeOf(exception)),
                    kv("errorMessage", exception.getMessage()), exception);
            return targets.stream()
                    .map(target -> failureResult(target, exception))
                    .toList();
        } catch (RuntimeException exception) {
            log.error(EXTERNAL_API, "FCM Multicast 클라이언트 예외 {}",
                    kv("exceptionType", exception.getClass().getSimpleName()), exception);
            return targets.stream()
                    .map(target -> FcmSendResult.failure(
                            target.referenceId(),
                            target.token(),
                            false,
                            false,
                            CLIENT_RUNTIME_EXCEPTION))
                    .toList();
        }
    }

    private List<FcmSendResult> mapResponse(BatchResponse response, List<FcmTarget> targets) {
        List<SendResponse> responses = response == null ? null : response.getResponses();
        List<FcmSendResult> results = new ArrayList<>(targets.size());

        // Admin SDK가 입력 토큰과 응답 순서를 보존하므로 같은 index의 발송 건에 결과를 대응한다.
        for (int index = 0; index < targets.size(); index++) {
            FcmTarget target = targets.get(index);
            if (responses == null || index >= responses.size() || responses.get(index) == null) {
                results.add(responseMismatch(target));
                continue;
            }

            SendResponse sendResponse = responses.get(index);
            if (sendResponse.isSuccessful()) {
                results.add(FcmSendResult.success(
                        target.referenceId(),
                        target.token(),
                        sendResponse.getMessageId()
                ));
                continue;
            }

            FirebaseMessagingException exception = sendResponse.getException();
            if (exception == null) {
                results.add(responseMismatch(target));
                continue;
            }

            FcmSendResult result = failureResult(target, exception);
            results.add(result);
            logSendFailure(target, exception, result);
        }

        return List.copyOf(results);
    }

    private FcmSendResult responseMismatch(FcmTarget target) {
        log.error(EXTERNAL_API, "FCM 응답과 발송 대상 불일치 {}",
                kv("referenceId", target.referenceId()));
        return FcmSendResult.failure(
                target.referenceId(),
                target.token(),
                true,
                false,
                CLIENT_RESPONSE_MISMATCH
        );
    }

    private FcmSendResult failureResult(FcmTarget target, FirebaseMessagingException exception) {
        Duration retryAfter = retryAfterOf(exception);
        MessagingErrorCode messagingErrorCode = exception.getMessagingErrorCode();
        if (messagingErrorCode != null) {
            return FcmSendResult.failure(
                    target.referenceId(),
                    target.token(),
                    RETRYABLE_MESSAGING_ERRORS.contains(messagingErrorCode),
                    messagingErrorCode == MessagingErrorCode.UNREGISTERED,
                    messagingErrorCode.name(),
                    retryAfter
            );
        }

        ErrorCode firebaseErrorCode = exception.getErrorCode();
        if (firebaseErrorCode == null) {
            return FcmSendResult.failure(
                    target.referenceId(),
                    target.token(),
                    true,
                    false,
                    ErrorCode.UNKNOWN.name(),
                    retryAfter
            );
        }

        return FcmSendResult.failure(
                target.referenceId(),
                target.token(),
                RETRYABLE_FIREBASE_ERRORS.contains(firebaseErrorCode),
                false,
                firebaseErrorCode.name(),
                retryAfter
        );
    }

    /** Retry-After의 delta-seconds와 HTTP-date 형식을 모두 지연 시간으로 변환한다. */
    private Duration retryAfterOf(FirebaseMessagingException exception) {
        if (exception.getHttpResponse() == null) {
            return null;
        }

        Object headerValue = exception.getHttpResponse().getHeaders().entrySet().stream()
                .filter(entry -> "Retry-After".equalsIgnoreCase(entry.getKey()))
                .map(java.util.Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        String retryAfter = firstHeaderValue(headerValue);
        if (retryAfter == null || retryAfter.isBlank()) {
            return null;
        }

        try {
            return Duration.ofSeconds(Math.max(0, Long.parseLong(retryAfter.trim())));
        } catch (NumberFormatException ignored) {
            try {
                Instant retryAt = ZonedDateTime.parse(
                        retryAfter.trim(),
                        DateTimeFormatter.RFC_1123_DATE_TIME
                ).toInstant();
                Duration delay = Duration.between(Instant.now(), retryAt);
                return delay.isNegative() ? Duration.ZERO : delay;
            } catch (DateTimeParseException invalidRetryAfter) {
                return null;
            }
        }
    }

    private String firstHeaderValue(Object headerValue) {
        if (headerValue instanceof Iterable<?> values) {
            var iterator = values.iterator();
            return iterator.hasNext() ? String.valueOf(iterator.next()) : null;
        }
        return headerValue == null ? null : String.valueOf(headerValue);
    }

    private String errorCodeOf(FirebaseMessagingException exception) {
        MessagingErrorCode messagingErrorCode = exception.getMessagingErrorCode();
        if (messagingErrorCode != null) {
            return messagingErrorCode.name();
        }
        return exception.getErrorCode() == null
                ? ErrorCode.UNKNOWN.name()
                : exception.getErrorCode().name();
    }

    private void logResultSummary(List<FcmSendResult> results) {
        long successCount = results.stream().filter(FcmSendResult::success).count();
        log.info(EXTERNAL_API, "FCM 알림 발송 완료 {} {} {}",
                kv("total", results.size()),
                kv("success", successCount),
                kv("failure", results.size() - successCount));
    }

    private void logSendFailure(
            FcmTarget target,
            FirebaseMessagingException exception,
            FcmSendResult result
    ) {
        int httpStatus = exception.getHttpResponse() == null
                ? 0
                : exception.getHttpResponse().getStatusCode();

        log.error(EXTERNAL_API, "FCM 발송 실패 {} {} {} {} {}",
                kv("token", maskToken(target.token())),
                kv("errorCode", result.errorCode()),
                kv("retryable", result.retryable()),
                kv("errorMessage", exception.getMessage()),
                kv("httpStatus", httpStatus));
    }

    private static String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 8) + "****";
    }

    @Transactional
    public void updateFcmToken(String userUuid, String deviceSecret, String requestToken) {

        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        List<FCM_Token> FCM_Tokens = fcmRepository.findByUser(user);

        if(FCM_Tokens.isEmpty()){
            FCM_Token newToken = FCM_Token.create(user, requestToken, "unknown");
            fcmRepository.save(newToken);
        } else {
            FCM_Token existingToken = FCM_Tokens.get(0);
            existingToken.updateToken(requestToken);
        }

    }

    @Transactional
    public void updateFcmTokenV2(String userUuid, String deviceSecret, String requestToken, String model) {

        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        List<FCM_Token> fcmTokens = fcmRepository.findByUser(user);

        if (fcmTokens.isEmpty()) {
            FCM_Token newToken = FCM_Token.create(user, requestToken, model);
            fcmRepository.save(newToken);
        } else {
            FCM_Token existingToken = fcmTokens.get(0);
            existingToken.updateToken(requestToken);
        }
    }

}
