package com.todaysound.todaysound_server.domain.summary.infra.scheduler;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.SCHEDULER;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.alarm.entity.DeliveryStatus;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryCleanupScheduler {

    private static final Set<DeliveryStatus> IN_FLIGHT_DELIVERY_STATUSES = Set.of(
            DeliveryStatus.PENDING,
            DeliveryStatus.PROCESSING,
            DeliveryStatus.RETRY
    );

    private final SummaryRepository summaryRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 실행
    public void deleteOldSummaries() {
        log.info(SCHEDULER, "Summary 정리 스케줄러 시작");
        long startTime = System.currentTimeMillis();

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        summaryRepository.deleteOldSummariesWithoutInFlightDeliveries(
                threshold,
                IN_FLIGHT_DELIVERY_STATUSES
        );

        long elapsed = System.currentTimeMillis() - startTime;
        log.info(SCHEDULER, "Summary 정리 스케줄러 완료 {} {}",
                kv("threshold", threshold),
                kv("elapsedMs", elapsed));
    }
}
