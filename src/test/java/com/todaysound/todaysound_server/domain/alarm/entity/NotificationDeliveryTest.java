package com.todaysound.todaysound_server.domain.alarm.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class NotificationDeliveryTest {

    @Test
    void create와_상태_전이는_발송_시도를_기록한다() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 10, 0, 0, 123_456_789);
        NotificationDelivery delivery = NotificationDelivery.create(
                mock(Summary.class),
                mock(FCM_Token.class),
                "a".repeat(64),
                now
        );

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(delivery.getAttemptCount()).isZero();
        assertThat(delivery.getNextAttemptAt()).isEqualTo(now.truncatedTo(ChronoUnit.MICROS));

        LocalDateTime firstLease = now.plusMinutes(5);
        delivery.claim(firstLease);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PROCESSING);
        assertThat(delivery.getAttemptCount()).isEqualTo(1);
        assertThat(delivery.isClaimedWith(firstLease)).isTrue();

        LocalDateTime retryAt = now.plusMinutes(1);
        delivery.markRetry("UNAVAILABLE", retryAt);
        delivery.claim(now.plusMinutes(7));

        assertThat(delivery.getAttemptCount()).isEqualTo(2);

        delivery.markSent("projects/test/messages/1", now.plusSeconds(10));

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(delivery.getFcmMessageId()).isEqualTo("projects/test/messages/1");
        assertThat(delivery.getSentAt()).isEqualTo(now.plusSeconds(10).truncatedTo(ChronoUnit.MICROS));
        assertThat(delivery.getLeaseUntil()).isNull();
    }

    @Test
    void 실패와_재시도는_오류와_다음_시각을_기록한다() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 10, 0);
        NotificationDelivery delivery = NotificationDelivery.create(
                mock(Summary.class),
                mock(FCM_Token.class),
                "b".repeat(64),
                now
        );
        delivery.claim(now.plusMinutes(5));

        delivery.markRetry("INTERNAL", now.plusMinutes(1));

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.RETRY);
        assertThat(delivery.getLastErrorCode()).isEqualTo("INTERNAL");
        assertThat(delivery.getNextAttemptAt()).isEqualTo(now.plusMinutes(1));
        assertThat(delivery.getLeaseUntil()).isNull();

        delivery.markFailed("INVALID_ARGUMENT");

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(delivery.getLastErrorCode()).isEqualTo("INVALID_ARGUMENT");
        assertThat(delivery.getNextAttemptAt()).isNull();
    }
}
