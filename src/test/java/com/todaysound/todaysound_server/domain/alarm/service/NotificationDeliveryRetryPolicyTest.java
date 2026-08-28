package com.todaysound.todaysound_server.domain.alarm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class NotificationDeliveryRetryPolicyTest {

    private final NotificationDeliveryRetryPolicy retryPolicy = new NotificationDeliveryRetryPolicy();

    @Test
    void 재시도는_1분_2분_4분의_지수_백오프를_사용한다() {
        assertThat(retryPolicy.delay(1, "UNAVAILABLE", 0)).isEqualTo(Duration.ofMinutes(1));
        assertThat(retryPolicy.delay(2, "INTERNAL", 0)).isEqualTo(Duration.ofMinutes(2));
        assertThat(retryPolicy.delay(3, "UNAVAILABLE", 0)).isEqualTo(Duration.ofMinutes(4));
    }

    @Test
    void jitter는_기본_지연의_최대_20퍼센트까지_더한다() {
        assertThat(retryPolicy.delay(1, "UNAVAILABLE", 0.20)).isEqualTo(Duration.ofSeconds(72));
        assertThat(retryPolicy.delay(2, "INTERNAL", 0.20)).isEqualTo(Duration.ofSeconds(144));
        assertThat(retryPolicy.delay(3, "UNAVAILABLE", 0.20)).isEqualTo(Duration.ofSeconds(288));
    }

    @Test
    void quota_exceeded의_첫_재시도는_최소_1분_뒤다() {
        assertThat(retryPolicy.delay(1, "QUOTA_EXCEEDED", 0)).isGreaterThanOrEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void retry_after가_백오프보다_길면_retry_after를_우선한다() {
        assertThat(retryPolicy.delay(
                1,
                "QUOTA_EXCEEDED",
                0,
                Duration.ofMinutes(3)
        )).isEqualTo(Duration.ofMinutes(3));
    }

    @Test
    void retry_after가_백오프보다_짧으면_백오프를_유지한다() {
        assertThat(retryPolicy.delay(
                3,
                "UNAVAILABLE",
                0,
                Duration.ofMinutes(1)
        )).isEqualTo(Duration.ofMinutes(4));
    }
}
