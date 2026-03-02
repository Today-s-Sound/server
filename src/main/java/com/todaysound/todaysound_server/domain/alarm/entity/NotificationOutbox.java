package com.todaysound.todaysound_server.domain.alarm.entity;

import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Transactional Outbox 패턴 구현체
 *
 * FCM 알림을 직접 전송하는 대신 이 테이블에 "전송할 것"을 기록한다.
 * Summary 저장과 같은 트랜잭션 내에서 저장되므로 둘 다 커밋되거나 둘 다 롤백된다.
 * NotificationOutboxScheduler 가 주기적으로 PENDING 건을 읽어 FCM 전송을 수행한다.
 */
@Entity
@Getter
@Table(name = "notification_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static NotificationOutbox create(Long userId, String title, String body) {
        NotificationOutbox outbox = new NotificationOutbox();
        outbox.userId = userId;
        outbox.title = title;
        outbox.body = body;
        outbox.status = OutboxStatus.PENDING;
        outbox.retryCount = 0;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }

    public void markAsSent() {
        this.status = OutboxStatus.SENT;
    }

    /**
     * 재시도 횟수를 증가시키고, maxRetry 초과 시 FAILED 로 전환한다.
     */
    public void incrementRetry(int maxRetry) {
        this.retryCount++;
        if (this.retryCount >= maxRetry) {
            this.status = OutboxStatus.FAILED;
        }
    }
}
