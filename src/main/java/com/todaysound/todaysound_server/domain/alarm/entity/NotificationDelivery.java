package com.todaysound.todaysound_server.domain.alarm.entity;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "notification_deliveries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_deliveries_summary_token",
                        columnNames = {"summary_id", "fcm_token_id"}
                ),
                @UniqueConstraint(
                        name = "uk_notification_deliveries_event_token",
                        columnNames = {"event_id", "fcm_token_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDelivery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = false)
    private Summary summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fcm_token_id", nullable = false)
    private FCM_Token fcmToken;

    @Column(name = "event_id", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "lease_until")
    private LocalDateTime leaseUntil;

    @Column(name = "last_error_code", length = 64)
    private String lastErrorCode;

    @Column(name = "fcm_message_id", length = 255)
    private String fcmMessageId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public static NotificationDelivery create(
            Summary summary,
            FCM_Token fcmToken,
            String eventId,
            LocalDateTime now
    ) {
        NotificationDelivery delivery = new NotificationDelivery();
        delivery.summary = Objects.requireNonNull(summary);
        delivery.fcmToken = Objects.requireNonNull(fcmToken);
        delivery.eventId = Objects.requireNonNull(eventId);
        delivery.status = DeliveryStatus.PENDING;
        delivery.attemptCount = 0;
        delivery.nextAttemptAt = truncateToMicros(now);
        delivery.createdAt = truncateToMicros(now);
        return delivery;
    }

    public void claim(LocalDateTime claimedLeaseUntil) {
        this.status = DeliveryStatus.PROCESSING;
        this.attemptCount++;
        this.nextAttemptAt = null;
        this.leaseUntil = truncateToMicros(claimedLeaseUntil);
    }

    public boolean isClaimedWith(LocalDateTime claimedLeaseUntil) {
        return status == DeliveryStatus.PROCESSING
                && Objects.equals(leaseUntil, truncateToMicros(claimedLeaseUntil));
    }

    public void markSent(String messageId, LocalDateTime now) {
        this.status = DeliveryStatus.SENT;
        this.nextAttemptAt = null;
        this.leaseUntil = null;
        this.fcmMessageId = messageId;
        this.sentAt = truncateToMicros(now);
    }

    public void markRetry(String errorCode, LocalDateTime retryAt) {
        this.status = DeliveryStatus.RETRY;
        this.nextAttemptAt = truncateToMicros(retryAt);
        this.leaseUntil = null;
        this.lastErrorCode = errorCode;
    }

    public void markFailed(String errorCode) {
        this.status = DeliveryStatus.FAILED;
        this.nextAttemptAt = null;
        this.leaseUntil = null;
        this.lastErrorCode = errorCode;
    }

    private static LocalDateTime truncateToMicros(LocalDateTime value) {
        return Objects.requireNonNull(value).truncatedTo(ChronoUnit.MICROS);
    }
}
