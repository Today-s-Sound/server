package com.todaysound.todaysound_server.domain.alarm.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeliveryRetryPolicy {

    private static final double MAX_JITTER_RATIO = 0.20;

    public LocalDateTime nextAttemptAt(
            LocalDateTime now,
            int attemptCount,
            String errorCode,
            Duration retryAfter
    ) {
        double jitterRatio = ThreadLocalRandom.current().nextDouble(MAX_JITTER_RATIO);
        return now.plus(delay(attemptCount, errorCode, jitterRatio, retryAfter))
                .truncatedTo(ChronoUnit.MICROS);
    }

    Duration delay(int attemptCount, String errorCode, double jitterRatio) {
        return delay(attemptCount, errorCode, jitterRatio, null);
    }

    Duration delay(
            int attemptCount,
            String errorCode,
            double jitterRatio,
            Duration retryAfter
    ) {
        int retryNumber = Math.max(1, Math.min(attemptCount, 3));
        Duration baseDelay = Duration.ofMinutes(1L << (retryNumber - 1));
        long jitterMillis = (long) (baseDelay.toMillis() * boundedJitter(jitterRatio));
        Duration backoff = baseDelay.plusMillis(jitterMillis);
        if (retryAfter == null || retryAfter.isNegative()) {
            return backoff;
        }
        // 서버 백오프보다 FCM Retry-After가 길면 공급자가 요구한 최소 대기 시간을 우선한다.
        return retryAfter.compareTo(backoff) > 0 ? retryAfter : backoff;
    }

    private double boundedJitter(double jitterRatio) {
        return Math.max(0, Math.min(jitterRatio, MAX_JITTER_RATIO));
    }
}
