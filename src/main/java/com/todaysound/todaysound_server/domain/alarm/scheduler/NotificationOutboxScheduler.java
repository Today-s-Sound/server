package com.todaysound.todaysound_server.domain.alarm.scheduler;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.alarm.entity.NotificationOutbox;
import com.todaysound.todaysound_server.domain.alarm.entity.OutboxStatus;
import com.todaysound.todaysound_server.domain.alarm.repository.NotificationOutboxRepository;
import com.todaysound.todaysound_server.domain.alarm.service.NotificationOutboxProcessor;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Transactional Outbox 패턴의 소비자(Consumer) 역할.
 *
 * notification_outbox 테이블에서 PENDING 상태인 항목을 주기적으로 읽어
 * NotificationOutboxProcessor 에 위임하여 FCM 전송을 수행한다.
 *
 * [처리 흐름]
 * PENDING → (전송 성공) → SENT
 * PENDING → (전송 실패 × MAX_RETRY) → FAILED
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

    private final NotificationOutboxRepository outboxRepository;
    private final UserRepository userRepository;
    private final NotificationOutboxProcessor outboxProcessor;

    /**
     * 5초마다 PENDING 상태인 outbox 항목을 처리한다.
     */
    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        List<NotificationOutbox> pending =
                outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pending.isEmpty()) {
            return;
        }

        log.info(BUSINESS, "Outbox 처리 시작 {}", kv("pendingCount", pending.size()));

        // N+1 방지: 필요한 User를 한 번의 쿼리로 일괄 조회
        Set<Long> userIds = pending.stream()
                .map(NotificationOutbox::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 아이템별로 별도 트랜잭션을 열어 처리 (Processor 에 위임)
        // 한 아이템의 실패가 다른 아이템에 영향을 주지 않는다
        for (NotificationOutbox outbox : pending) {
            outboxProcessor.process(outbox.getId(), userMap.get(outbox.getUserId()));
        }
    }
}
