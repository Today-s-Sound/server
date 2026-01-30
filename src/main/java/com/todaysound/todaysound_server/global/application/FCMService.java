package com.todaysound.todaysound_server.global.application;

import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import static com.todaysound.todaysound_server.global.utils.LogMarkers.EXTERNAL_API;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FCMRepository fcmRepository;
    private final HeaderAuthValidator headerAuthValidator;
    private final FirebaseMessagingClient firebaseMessagingClient;

    /**
     * (핵심 메소드) 특정 User에게 알림을 발송합니다.
     *
     * @param user  알림을 받을 User 엔티티
     * @param title 알림 제목
     * @param body  알림 본문
     */
    @Transactional
    public void sendNotificationToUser(User user, String title, String body) {

        List<FCM_Token> devices = fcmRepository.findByUser(user);

        if (devices.isEmpty()) {
            log.warn(EXTERNAL_API, "알림을 보낼 기기 토큰 없음 {}", kv("userId", user.getId()));
            return;
        }

        // FCM 토큰 문자열만 추출
        List<String> tokens = devices.stream().map(FCM_Token::getFcmToken).collect(Collectors.toList());

        // 알림 메시지 내용 구성
        Notification notification = Notification.builder().setTitle(title).setBody(body).build();

        ApnsConfig apnsConfig = ApnsConfig.builder().putHeader("apns-priority", "10")
                .setAps(Aps.builder().setSound("default").setBadge(1).build()).build();

        // 여러 토큰에 한 번에 보내는 MulticastMessage 구성
        MulticastMessage message = MulticastMessage.builder().setNotification(notification).setApnsConfig(apnsConfig)
                .addAllTokens(tokens).build();

        // FCM에 일괄 발송 요청
        BatchResponse response;
        try {
            response = firebaseMessagingClient.sendEachForMulticast(message);

            log.info(EXTERNAL_API, "FCM 알림 발송 완료 {} {} {}",
                    kv("total", response.getSuccessCount() + response.getFailureCount()),
                    kv("success", response.getSuccessCount()),
                    kv("failure", response.getFailureCount()));

            if (response.getFailureCount() > 0) {
                handleFailedTokens(response, tokens);
            }

        } catch (FirebaseMessagingException e) {
            log.error(EXTERNAL_API, "FCM Multicast 발송 실패 {} {}",
                    kv("userId", user.getId()),
                    kv("errorMessage", e.getMessage()), e);
        }
    }

    /**
     * 발송 실패(특히 UNREGISTERED) 응답을 받은 토큰을 DB에서 삭제
     */
    private void handleFailedTokens(BatchResponse response, List<String> originalTokens) {
        List<String> tokensToDelete = new ArrayList<>();

        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);

            // 발송 실패한 경우 처리
            if (!sendResponse.isSuccessful()) {

                String failedToken = originalTokens.get(i);
                FirebaseMessagingException exception = sendResponse.getException();

                MessagingErrorCode errorCode = exception.getMessagingErrorCode(); // Enum 값
                String errorMessage = exception.getMessage(); // 실제 에러 내용

                String maskedToken = maskToken(failedToken);
                int httpStatus = exception.getHttpResponse() != null
                        ? exception.getHttpResponse().getStatusCode() : 0;

                log.error(EXTERNAL_API, "FCM 발송 실패 {} {} {} {}",
                        kv("token", maskedToken),
                        kv("errorCode", errorCode),
                        kv("errorMessage", errorMessage),
                        kv("httpStatus", httpStatus));

                if (errorCode == MessagingErrorCode.UNREGISTERED) {
                    log.warn(EXTERNAL_API, "만료된 FCM 토큰 삭제 대상 추가 {}", kv("token", maskedToken));
                    tokensToDelete.add(failedToken);
                }
            }
        }

        // 삭제할 토큰이 있다면 DB에서 일괄 삭제
        if (!tokensToDelete.isEmpty()) {
            fcmRepository.deleteAllByFcmTokenIn(tokensToDelete);
            log.info(EXTERNAL_API, "만료된 FCM 토큰 DB 삭제 완료 {}", kv("deletedCount", tokensToDelete.size()));
        }
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
            if (!existingToken.equals(requestToken)) {
                existingToken.updateToken(requestToken);
            }
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
            if (!existingToken.getFcmToken().equals(requestToken)) {
                existingToken.updateToken(requestToken);
            }
        }
    }

}
