package com.todaysound.todaysound_server.domain.alarm.service;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.alarm.entity.NotificationOutbox;
import com.todaysound.todaysound_server.domain.alarm.entity.OutboxStatus;
import com.todaysound.todaysound_server.domain.alarm.repository.NotificationOutboxRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.global.application.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox 항목 하나를 처리하는 단위 컴포넌트.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxProcessor {

    private static final int MAX_RETRY = 3;

    private final NotificationOutboxRepository outboxRepository;
    private final FCMService fcmService;

    /**
     * outbox 항목 하나를 처리한다. 스케줄러가 아이템별로 호출한다.
     *
     * @param outboxId 처리할 outbox의 PK
     * @param user     스케줄러에서 batch 조회한 User (탈퇴 사용자인 경우 null)
     */
    @Transactional
    public void process(Long outboxId, User user) {
        // detached 상태의 엔티티를 다시 로드해 managed 상태로 전환
        NotificationOutbox outbox = outboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalStateException("Outbox not found: " + outboxId));

        // 탈퇴한 사용자: 재시도 없이 즉시 FAILED
        if (user == null) {
            log.warn(BUSINESS, "Outbox 전송 대상 사용자 없음 (탈퇴) {} {}",
                    kv("outboxId", outboxId),
                    kv("userId", outbox.getUserId()));
            outbox.markAsFailed();
            return;
        }

        try {
            fcmService.sendNotificationToUser(user, outbox.getTitle(), outbox.getBody());
            outbox.markAsSent();

            log.info(BUSINESS, "Outbox FCM 전송 성공 {} {}",
                    kv("outboxId", outboxId),
                    kv("userId", outbox.getUserId()));

        } catch (Exception e) {
            log.warn(BUSINESS, "Outbox FCM 전송 실패 {} {} {}",
                    kv("outboxId", outboxId),
                    kv("retryCount", outbox.getRetryCount()),
                    kv("error", e.getMessage()));

            outbox.incrementRetry(MAX_RETRY);

            if (outbox.getStatus() == OutboxStatus.FAILED) {
                log.error(BUSINESS, "Outbox 최대 재시도 초과, FAILED 처리 {} {}",
                        kv("outboxId", outboxId),
                        kv("userId", outbox.getUserId()));
            }
        }
    }
}
