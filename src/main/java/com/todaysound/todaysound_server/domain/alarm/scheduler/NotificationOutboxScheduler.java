package com.todaysound.todaysound_server.domain.alarm.scheduler;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.alarm.entity.NotificationOutbox;
import com.todaysound.todaysound_server.domain.alarm.entity.OutboxStatus;
import com.todaysound.todaysound_server.domain.alarm.repository.NotificationOutboxRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.global.application.FCMService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional Outbox 패턴의 소비자(Consumer) 역할.
 *
 * notification_outbox 테이블에서 PENDING 상태인 항목을 주기적으로 읽어 FCM 전송을 수행한다.
 * FCM 전송에 실패하면 retryCount 를 증가시키며, MAX_RETRY 초과 시 FAILED 로 전환한다.
 *
 * [처리 흐름]
 * PENDING → (전송 성공) → SENT
 * PENDING → (전송 실패 × MAX_RETRY) → FAILED
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

    private static final int MAX_RETRY = 3;

    private final NotificationOutboxRepository outboxRepository;
    private final FCMService fcmService;
    private final UserRepository userRepository;

    /**
     * 5초마다 PENDING 상태인 outbox 항목을 처리한다.
     *
     * FCMService.sendNotificationToUser() 는 내부적으로 FirebaseMessagingException 을 catch 하므로
     * Firebase 장애 시에도 예외가 전파되지 않는다. 단, DB 접근 실패 등 인프라 레벨 오류는
     * 트랜잭션 전체를 롤백시켜 다음 주기에 재처리된다.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        List<NotificationOutbox> pending =
                outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pending.isEmpty()) {
            return;
        }

        log.info(BUSINESS, "Outbox 처리 시작 {}", kv("pendingCount", pending.size()));

        for (NotificationOutbox outbox : pending) {
            try {
                User user = userRepository.findById(outbox.getUserId()).orElse(null);

                if (user == null) {
                    // 탈퇴한 사용자: 재시도 없이 즉시 FAILED 처리
                    log.warn(BUSINESS, "Outbox 전송 대상 사용자 없음 (탈퇴) {} {}",
                            kv("outboxId", outbox.getId()),
                            kv("userId", outbox.getUserId()));
                    outbox.incrementRetry(1);
                    continue;
                }

                fcmService.sendNotificationToUser(user, outbox.getTitle(), outbox.getBody());
                outbox.markAsSent();

                log.info(BUSINESS, "Outbox FCM 전송 성공 {} {}",
                        kv("outboxId", outbox.getId()),
                        kv("userId", outbox.getUserId()));

            } catch (Exception e) {
                log.warn(BUSINESS, "Outbox FCM 전송 실패 {} {} {}",
                        kv("outboxId", outbox.getId()),
                        kv("retryCount", outbox.getRetryCount()),
                        kv("error", e.getMessage()));

                outbox.incrementRetry(MAX_RETRY);

                if (outbox.getStatus() == OutboxStatus.FAILED) {
                    log.error(BUSINESS, "Outbox 최대 재시도 초과, FAILED 처리 {} {}",
                            kv("outboxId", outbox.getId()),
                            kv("userId", outbox.getUserId()));
                }
            }
        }
    }
}
