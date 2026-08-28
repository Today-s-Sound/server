package com.todaysound.todaysound_server.domain.alarm.service;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.alarm.service.NotificationDeliveryService.ClaimedDelivery;
import com.todaysound.todaysound_server.domain.alarm.service.NotificationDeliveryService.DeliveryResult;
import com.todaysound.todaysound_server.global.application.FCMService;
import com.todaysound.todaysound_server.global.application.FcmSendResult;
import com.todaysound.todaysound_server.global.application.FcmTarget;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 선점 트랜잭션을 끝낸 뒤 FCM을 호출하고, 결과는 별도 트랜잭션으로 저장한다.
 * 외부 호출 중에는 DB 잠금과 커넥션을 점유하지 않는다. FCM 성공 직후 프로세스가 종료되면
 * lease 만료 후 재발송될 수 있으므로 전체 전달 보장은 exactly-once가 아닌 at-least-once다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeliveryDispatcher {

    private static final String CALL_FAILURE_ERROR = "FCM_CALL_FAILED";
    private static final String MISSING_RESPONSE_ERROR = "MISSING_FCM_RESPONSE";

    private final NotificationDeliveryService deliveryService;
    private final FCMService fcmService;

    public int dispatchPendingDeliveries() {
        List<ClaimedDelivery> claimed = deliveryService.claimBatch(NotificationDeliveryService.MAX_BATCH_SIZE);
        if (claimed.isEmpty()) {
            return 0;
        }

        // 하나의 Multicast는 payload를 공유하므로 메시지 내용과 eventId가 모두 같은 작업만 묶는다.
        Map<MessageKey, List<ClaimedDelivery>> groups = claimed.stream()
                .collect(Collectors.groupingBy(
                        delivery -> new MessageKey(
                                delivery.title(),
                                delivery.body(),
                                delivery.eventId()
                        ),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        groups.forEach(this::sendGroup);
        return claimed.size();
    }

    private void sendGroup(MessageKey message, List<ClaimedDelivery> deliveries) {
        List<FcmTarget> targets = deliveries.stream()
                .map(delivery -> new FcmTarget(delivery.deliveryId(), delivery.token()))
                .toList();

        List<FcmSendResult> sendResults;
        try {
            sendResults = fcmService.sendMulticast(
                    message.title(),
                    message.body(),
                    message.eventId(),
                    targets
            );
        } catch (RuntimeException exception) {
            log.error(BUSINESS, "Notification delivery FCM call failed {} {}",
                    kv("deliveryCount", deliveries.size()),
                    kv("error", exception.getMessage()), exception);
            deliveryService.applyResults(deliveries.stream()
                    .map(delivery -> failedResult(delivery, CALL_FAILURE_ERROR, false))
                    .toList());
            return;
        }

        Map<Long, FcmSendResult> resultByDeliveryId = sendResults.stream()
                .collect(Collectors.toMap(
                        FcmSendResult::referenceId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        List<DeliveryResult> results = new ArrayList<>(deliveries.size());
        for (ClaimedDelivery delivery : deliveries) {
            FcmSendResult sendResult = resultByDeliveryId.get(delivery.deliveryId());
            if (sendResult == null) {
                results.add(failedResult(delivery, MISSING_RESPONSE_ERROR, true));
                continue;
            }
            results.add(new DeliveryResult(
                    delivery.deliveryId(),
                    delivery.leaseUntil(),
                    sendResult.attemptedToken(),
                    sendResult.success(),
                    sendResult.retryable(),
                    sendResult.unregistered(),
                    sendResult.messageId(),
                    sendResult.errorCode(),
                    sendResult.retryAfter()
            ));
        }
        deliveryService.applyResults(results);
    }

    private DeliveryResult failedResult(
            ClaimedDelivery delivery,
            String errorCode,
            boolean retryable
    ) {
        return new DeliveryResult(
                delivery.deliveryId(),
                delivery.leaseUntil(),
                delivery.token(),
                false,
                retryable,
                false,
                null,
                errorCode,
                null
        );
    }

    private record MessageKey(String title, String body, String eventId) {
    }
}
