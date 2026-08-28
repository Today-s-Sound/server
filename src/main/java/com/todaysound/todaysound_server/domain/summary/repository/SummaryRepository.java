package com.todaysound.todaysound_server.domain.summary.repository;

import com.todaysound.todaysound_server.domain.alarm.entity.DeliveryStatus;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {

    /**
     * Summary ID로 조회
     */
    Optional<Summary> findById(Long id);

    Optional<Summary> findFirstBySubscriptionIdAndHashOrderByIdAsc(Long subscriptionId, String hash);

    /**
     * 생성일 기준으로 오래된 Summary 삭제
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            DELETE FROM Summary summary
            WHERE summary.createdAt < :dateTime
              AND NOT EXISTS (
                    SELECT delivery.id
                    FROM NotificationDelivery delivery
                    WHERE delivery.summary = summary
                      AND delivery.status IN :protectedStatuses
              )
            """)
    void deleteOldSummariesWithoutInFlightDeliveries(
            @Param("dateTime") LocalDateTime dateTime,
            @Param("protectedStatuses") Collection<DeliveryStatus> protectedStatuses
    );

}
