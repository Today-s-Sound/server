package com.todaysound.todaysound_server.domain.alarm.service;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.alarm.entity.DeliveryStatus;
import com.todaysound.todaysound_server.domain.alarm.entity.NotificationDelivery;
import com.todaysound.todaysound_server.domain.alarm.repository.NotificationDeliveryRepository;
import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    // 최초 전송 1회와 최대 3회의 재시도를 합한 횟수다.
    public static final int MAX_ATTEMPTS = 4;
    public static final int MAX_BATCH_SIZE = 500;
    private static final Duration LEASE_DURATION = Duration.ofMinutes(5);
    private static final String INACTIVE_TOKEN_ERROR = "TOKEN_INACTIVE";
    private static final String EXHAUSTED_LEASE_ERROR = "LEASE_EXPIRED_AFTER_MAX_ATTEMPTS";

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationDeliveryRetryPolicy retryPolicy;
    private final FCMRepository fcmRepository;

    @Transactional
    public List<ClaimedDelivery> claimBatch(int requestedBatchSize) {
        return claimBatch(requestedBatchSize, LocalDateTime.now());
    }

    @Transactional
    public List<ClaimedDelivery> claimBatch(int requestedBatchSize, LocalDateTime requestedAt) {
        int batchSize = Math.max(1, Math.min(requestedBatchSize, MAX_BATCH_SIZE));
        LocalDateTime now = truncateToMicros(requestedAt);
        // 후보 행 잠금부터 PROCESSING 전환까지 한 트랜잭션으로 묶어 선점을 원자적으로 만든다.
        List<Long> eligibleIds = deliveryRepository.findEligibleIdsForUpdate(now, batchSize);
        if (eligibleIds.isEmpty()) {
            return List.of();
        }

        Map<Long, NotificationDelivery> deliveriesById = deliveryRepository
                .findAllForDispatchByIdIn(eligibleIds)
                .stream()
                .collect(Collectors.toMap(
                        NotificationDelivery::getId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        LocalDateTime leaseUntil = truncateToMicros(now.plus(LEASE_DURATION));
        return eligibleIds.stream()
                .map(deliveriesById::get)
                .filter(Objects::nonNull)
                .map(delivery -> claim(delivery, leaseUntil))
                .filter(Objects::nonNull)
                .toList();
    }

    private ClaimedDelivery claim(NotificationDelivery delivery, LocalDateTime leaseUntil) {
        if (delivery.getAttemptCount() >= MAX_ATTEMPTS) {
            delivery.markFailed(EXHAUSTED_LEASE_ERROR);
            log.error(BUSINESS, "Notification delivery expired after max attempts {} {}",
                    kv("deliveryId", delivery.getId()),
                    kv("attemptCount", delivery.getAttemptCount()));
            return null;
        }

        delivery.claim(leaseUntil);
        if (!delivery.getFcmToken().isActive()) {
            delivery.markFailed(INACTIVE_TOKEN_ERROR);
            log.warn(BUSINESS, "Notification delivery skipped inactive token {} {}",
                    kv("deliveryId", delivery.getId()),
                    kv("tokenId", delivery.getFcmToken().getId()));
            return null;
        }

        return new ClaimedDelivery(
                delivery.getId(),
                delivery.getFcmToken().getId(),
                delivery.getFcmToken().getFcmToken(),
                "[" + delivery.getSummary().getSubscription().getAlias() + "] "
                        + delivery.getSummary().getTitle(),
                delivery.getSummary().getContent(),
                delivery.getEventId(),
                delivery.getLeaseUntil()
        );
    }

    @Transactional
    public void applyResults(Collection<DeliveryResult> results) {
        applyResults(results, LocalDateTime.now());
    }

    @Transactional
    public void applyResults(Collection<DeliveryResult> results, LocalDateTime completedAt) {
        if (results.isEmpty()) {
            return;
        }

        Map<Long, DeliveryResult> resultByDeliveryId = results.stream()
                .collect(Collectors.toMap(
                        DeliveryResult::deliveryId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        // 행 잠금 아래에서 lease를 검사해야 늦은 결과와 만료 작업의 재선점이 서로 덮어쓰지 않는다.
        List<NotificationDelivery> processingDeliveries = deliveryRepository.findAllByStatusAndIdIn(
                DeliveryStatus.PROCESSING,
                resultByDeliveryId.keySet()
        );
        LocalDateTime now = truncateToMicros(completedAt);

        for (NotificationDelivery delivery : processingDeliveries) {
            DeliveryResult result = resultByDeliveryId.get(delivery.getId());
            if (!delivery.isClaimedWith(result.claimedLeaseUntil())) {
                continue;
            }
            applyResult(delivery, result, now);
        }
    }

    private void applyResult(NotificationDelivery delivery, DeliveryResult result, LocalDateTime now) {
        if (result.success()) {
            delivery.markSent(result.messageId(), now);
            log.debug(BUSINESS, "Notification delivery sent {} {}",
                    kv("deliveryId", delivery.getId()),
                    kv("attemptCount", delivery.getAttemptCount()));
            return;
        }

        String errorCode = normalizedErrorCode(result.errorCode());
        if (result.unregistered()) {
            delivery.markFailed(errorCode);
            // 발송 중 토큰이 갱신됐을 수 있으므로 실제 시도한 토큰과 현재 값이 같을 때만 끈다.
            int deactivatedCount = fcmRepository.deactivateIfTokenMatches(
                    delivery.getFcmToken().getId(),
                    result.attemptedToken(),
                    now
            );
            log.warn(BUSINESS, "Notification delivery token unregistered {} {} {} {}",
                    kv("deliveryId", delivery.getId()),
                    kv("tokenId", delivery.getFcmToken().getId()),
                    kv("tokenDeactivated", deactivatedCount > 0),
                    kv("errorCode", errorCode));
            return;
        }

        if (!result.retryable() || delivery.getAttemptCount() >= MAX_ATTEMPTS) {
            delivery.markFailed(errorCode);
            log.error(BUSINESS, "Notification delivery permanently failed {} {} {} {}",
                    kv("deliveryId", delivery.getId()),
                    kv("attemptCount", delivery.getAttemptCount()),
                    kv("errorCode", errorCode),
                    kv("retryable", result.retryable()));
            return;
        }

        LocalDateTime nextAttemptAt = retryPolicy.nextAttemptAt(
                now,
                delivery.getAttemptCount(),
                errorCode,
                result.retryAfter()
        );
        delivery.markRetry(errorCode, nextAttemptAt);
        log.warn(BUSINESS, "Notification delivery scheduled for retry {} {} {} {}",
                kv("deliveryId", delivery.getId()),
                kv("attemptCount", delivery.getAttemptCount()),
                kv("errorCode", errorCode),
                kv("nextAttemptAt", nextAttemptAt));
    }

    private String normalizedErrorCode(String errorCode) {
        return errorCode == null || errorCode.isBlank() ? "UNKNOWN" : errorCode;
    }

    private static LocalDateTime truncateToMicros(LocalDateTime value) {
        return Objects.requireNonNull(value).truncatedTo(ChronoUnit.MICROS);
    }

    public record ClaimedDelivery(
            Long deliveryId,
            Long tokenId,
            String token,
            String title,
            String body,
            String eventId,
            LocalDateTime leaseUntil
    ) {
    }

    public record DeliveryResult(
            Long deliveryId,
            LocalDateTime claimedLeaseUntil,
            String attemptedToken,
            boolean success,
            boolean retryable,
            boolean unregistered,
            String messageId,
            String errorCode,
            Duration retryAfter
    ) {
    }
}
