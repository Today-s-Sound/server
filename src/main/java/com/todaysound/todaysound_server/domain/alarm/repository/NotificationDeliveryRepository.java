package com.todaysound.todaysound_server.domain.alarm.repository;

import com.todaysound.todaysound_server.domain.alarm.entity.DeliveryStatus;
import com.todaysound.todaysound_server.domain.alarm.entity.NotificationDelivery;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO notification_deliveries (
                summary_id,
                fcm_token_id,
                event_id,
                status,
                attempt_count,
                next_attempt_at,
                created_at
            ) VALUES (
                :summaryId,
                :fcmTokenId,
                :eventId,
                'PENDING',
                0,
                :createdAt,
                :createdAt
            )
            ON DUPLICATE KEY UPDATE id = notification_deliveries.id
            """, nativeQuery = true)
    int insertPendingIfAbsent(
            @Param("summaryId") Long summaryId,
            @Param("fcmTokenId") Long fcmTokenId,
            @Param("eventId") String eventId,
            @Param("createdAt") LocalDateTime createdAt
    );

    @Query(value = """
            SELECT delivery.id
            FROM notification_deliveries delivery
            WHERE (
                    delivery.status IN ('PENDING', 'RETRY')
                    AND delivery.next_attempt_at <= :now
                )
                OR (
                    delivery.status = 'PROCESSING'
                    AND delivery.lease_until <= :now
                )
            ORDER BY
                CASE
                    WHEN delivery.status = 'PROCESSING' THEN delivery.lease_until
                    ELSE delivery.next_attempt_at
                END,
                delivery.id
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Long> findEligibleIdsForUpdate(
            @Param("now") LocalDateTime now,
            @Param("batchSize") int batchSize
    );

    @Query("""
            SELECT DISTINCT delivery
            FROM NotificationDelivery delivery
            JOIN FETCH delivery.summary summary
            JOIN FETCH summary.subscription
            JOIN FETCH delivery.fcmToken
            WHERE delivery.id IN :ids
            """)
    List<NotificationDelivery> findAllForDispatchByIdIn(@Param("ids") Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT delivery
            FROM NotificationDelivery delivery
            JOIN FETCH delivery.fcmToken
            WHERE delivery.status = :status
              AND delivery.id IN :ids
            """)
    List<NotificationDelivery> findAllByStatusAndIdIn(
            @Param("status") DeliveryStatus status,
            @Param("ids") Collection<Long> ids
    );
}
