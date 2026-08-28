package com.todaysound.todaysound_server.command.summary.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.todaysound.todaysound_server.domain.alarm.entity.DeliveryStatus;
import com.todaysound.todaysound_server.domain.alarm.entity.NotificationDelivery;
import com.todaysound.todaysound_server.domain.alarm.repository.NotificationDeliveryRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.infra.scheduler.SummaryCleanupScheduler;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.url.entity.Url;
import com.todaysound.todaysound_server.domain.url.repository.UrlRepository;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.entity.UserType;
import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.support.ServiceTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class SummaryCleanupSchedulerTest extends ServiceTestSupport {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private SummaryCleanupScheduler summaryCleanupScheduler;

    @Autowired
    private FCMRepository fcmRepository;

    @Autowired
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @Test
    void _7일_지난_요약은_삭제한다() {
        // given
        Url url = Url.create("http://example.com", "Example Site");
        urlRepository.save(url);

        User user = User.create("userId1", "hashedSecret", "fingerPrint", UserType.USER, true, "plainSecret");
        userRepository.save(user);

        Subscription subscription = Subscription.create(url, true, "alias", user, "lastSeenPostId");
        subscriptionRepository.save(subscription);

        Summary oldSummary = Summary.create("hash", "title", "content", "postUrl", "postDate", true, subscription);
        Summary newSummary = Summary.create("hash2", "title2", "content2", "postUrl2", "postDate2", false, subscription);

        ReflectionTestUtils.setField(oldSummary, "createdAt", LocalDateTime.now().minusDays(8));
        ReflectionTestUtils.setField(newSummary, "createdAt", LocalDateTime.now().minusDays(6));
        summaryRepository.saveAll(List.of(oldSummary, newSummary));

        // when
        summaryCleanupScheduler.deleteOldSummaries();

        // then
        List<Summary> remaining = summaryRepository.findAll();

        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getTitle()).isEqualTo("title2");
    }

    @Test
    void 비종결_Delivery가_있는_7일_지난_요약은_삭제하지_않는다() {
        // given
        Url url = urlRepository.save(Url.create("http://delivery.example.com", "Delivery Site"));
        User user = userRepository.save(User.create(
                "delivery-user",
                "hashedSecret",
                "delivery-fingerprint",
                UserType.USER,
                true,
                "plainSecret"
        ));
        Subscription subscription = subscriptionRepository.save(
                Subscription.create(url, true, "alias", user, "lastSeenPostId")
        );
        FCM_Token token = fcmRepository.save(FCM_Token.create(user, "cleanup-token", "iPhone"));
        LocalDateTime oldCreatedAt = LocalDateTime.now().minusDays(8);

        Summary withoutDelivery = saveOldSummary(subscription, "no-delivery", oldCreatedAt);
        Summary pendingSummary = saveOldSummary(subscription, "pending", oldCreatedAt);
        Summary processingSummary = saveOldSummary(subscription, "processing", oldCreatedAt);
        Summary retrySummary = saveOldSummary(subscription, "retry", oldCreatedAt);
        Summary sentSummary = saveOldSummary(subscription, "sent", oldCreatedAt);
        Summary failedSummary = saveOldSummary(subscription, "failed", oldCreatedAt);

        NotificationDelivery pending = createDelivery(pendingSummary, token, 1, oldCreatedAt);
        NotificationDelivery processing = createDelivery(processingSummary, token, 2, oldCreatedAt);
        processing.claim(LocalDateTime.now().plusMinutes(5));
        NotificationDelivery retry = createDelivery(retrySummary, token, 3, oldCreatedAt);
        retry.claim(LocalDateTime.now().plusMinutes(5));
        retry.markRetry("UNAVAILABLE", LocalDateTime.now().plusMinutes(1));
        NotificationDelivery sent = createDelivery(sentSummary, token, 4, oldCreatedAt);
        sent.claim(LocalDateTime.now().plusMinutes(5));
        sent.markSent("message-id", LocalDateTime.now());
        NotificationDelivery failed = createDelivery(failedSummary, token, 5, oldCreatedAt);
        failed.claim(LocalDateTime.now().plusMinutes(5));
        failed.markFailed("INVALID_ARGUMENT");
        notificationDeliveryRepository.saveAll(List.of(pending, processing, retry, sent, failed));

        // when
        summaryCleanupScheduler.deleteOldSummaries();

        // then
        assertThat(summaryRepository.findAll())
                .extracting(Summary::getId)
                .containsExactlyInAnyOrder(
                        pendingSummary.getId(),
                        processingSummary.getId(),
                        retrySummary.getId()
                )
                .doesNotContain(withoutDelivery.getId(), sentSummary.getId(), failedSummary.getId());
        assertThat(notificationDeliveryRepository.findAll())
                .extracting(NotificationDelivery::getStatus)
                .containsExactlyInAnyOrder(
                        DeliveryStatus.PENDING,
                        DeliveryStatus.PROCESSING,
                        DeliveryStatus.RETRY
                );
    }

    private Summary saveOldSummary(
            Subscription subscription,
            String hash,
            LocalDateTime createdAt
    ) {
        Summary summary = Summary.create(
                hash,
                hash + " title",
                hash + " content",
                "https://example.com/" + hash,
                "2026-08-20",
                true,
                subscription
        );
        ReflectionTestUtils.setField(summary, "createdAt", createdAt);
        return summaryRepository.save(summary);
    }

    private NotificationDelivery createDelivery(
            Summary summary,
            FCM_Token token,
            int sequence,
            LocalDateTime createdAt
    ) {
        return NotificationDelivery.create(
                summary,
                token,
                String.format("%064x", sequence),
                createdAt
        );
    }

}
