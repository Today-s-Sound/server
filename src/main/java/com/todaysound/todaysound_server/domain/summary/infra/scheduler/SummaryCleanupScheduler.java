package com.todaysound.todaysound_server.domain.summary.infra.scheduler;

import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SummaryCleanupScheduler {

    private final SummaryRepository summaryRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 실행
    public void deleteOldSummaries() {

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        summaryRepository.deleteByCreatedAtBefore(threshold);

    }
}
