package com.todaysound.todaysound_server.command.alarm.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import com.todaysound.todaysound_server.domain.alarm.entity.NotificationDelivery;
import com.todaysound.todaysound_server.domain.alarm.repository.NotificationDeliveryRepository;
import com.todaysound.todaysound_server.domain.alarm.service.InternalAlertCommand;
import com.todaysound.todaysound_server.domain.alarm.service.InternalAlertService;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.url.entity.Url;
import com.todaysound.todaysound_server.domain.url.repository.UrlRepository;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.entity.UserType;
import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.global.utils.CryptoUtils;
import com.todaysound.todaysound_server.support.ServiceTestSupport;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InternalAlertServiceTest extends ServiceTestSupport {

    @Autowired
    private InternalAlertService internalAlertService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private FCMRepository fcmRepository;

    @Autowired
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @Test
    void 알림을_생성하면_Summary와_활성_토큰별_Delivery를_함께_저장한다() {
        AlertFixture fixture = createAlertFixture();
        FCM_Token inactiveToken = FCM_Token.create(fixture.user(), "inactive-token", "iPhone 14");
        inactiveToken.deactivate();
        fcmRepository.save(inactiveToken);
        InternalAlertCommand command = createCommand(fixture);

        internalAlertService.createAlert(command);

        Summary summary = summaryRepository.findFirstBySubscriptionIdAndHashOrderByIdAsc(
                fixture.subscription().getId(), command.sitePostId()).orElseThrow();
        List<NotificationDelivery> deliveries = notificationDeliveryRepository.findAll();

        assertThat(summary.getTitle()).isEqualTo(command.title());
        assertThat(deliveries).singleElement().satisfies(delivery -> {
            assertThat(delivery.getSummary().getId()).isEqualTo(summary.getId());
            assertThat(delivery.getFcmToken().getId()).isEqualTo(fixture.activeToken().getId());
            assertThat(delivery.getEventId()).isEqualTo(CryptoUtils.sha256(
                    fixture.subscription().getUrl().getId() + ":" + command.sitePostId()));
        });
        verifyNoInteractions(firebaseMessagingClient);
    }

    @Test
    void 같은_구독과_게시글을_다시_요청하면_Summary와_Delivery를_중복_생성하지_않는다() {
        AlertFixture fixture = createAlertFixture();
        InternalAlertCommand command = createCommand(fixture);

        internalAlertService.createAlert(command);
        internalAlertService.createAlert(command);

        assertThat(summaryRepository.count()).isEqualTo(1);
        assertThat(notificationDeliveryRepository.count()).isEqualTo(1);
        verifyNoInteractions(firebaseMessagingClient);
    }

    @Test
    void 같은_사용자가_같은_사이트를_중복_구독해도_같은_글은_토큰당_한_번만_발송한다() {
        AlertFixture fixture = createAlertFixture();
        Subscription secondSubscription = subscriptionRepository.save(
                Subscription.create(
                        fixture.subscription().getUrl(),
                        true,
                        "다른 별칭",
                        fixture.user(),
                        "previous-post"
                ));
        InternalAlertCommand first = createCommand(fixture);
        InternalAlertCommand second = new InternalAlertCommand(
                fixture.user().getId(),
                secondSubscription.getId(),
                first.sitePostId(),
                first.title(),
                first.url(),
                first.publishedAt(),
                first.contentSummary(),
                first.keywordMatched()
        );

        internalAlertService.createAlert(first);
        internalAlertService.createAlert(second);

        assertThat(summaryRepository.count()).isEqualTo(2);
        assertThat(notificationDeliveryRepository.count()).isEqualTo(1);
        verifyNoInteractions(firebaseMessagingClient);
    }

    @Test
    void 중복_구독_callback이_동시에_와도_각_Summary는_저장하고_Delivery만_중복_제거한다() throws Exception {
        AlertFixture fixture = createAlertFixture();
        Subscription secondSubscription = subscriptionRepository.save(
                Subscription.create(
                        fixture.subscription().getUrl(),
                        true,
                        "다른 별칭",
                        fixture.user(),
                        "previous-post"
                ));
        InternalAlertCommand first = createCommand(fixture);
        InternalAlertCommand second = new InternalAlertCommand(
                fixture.user().getId(),
                secondSubscription.getId(),
                first.sitePostId(),
                first.title(),
                first.url(),
                first.publishedAt(),
                first.contentSummary(),
                first.keywordMatched()
        );
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstFuture = executor.submit(() -> createAlertAfterSignal(first, ready, start));
            Future<?> secondFuture = executor.submit(() -> createAlertAfterSignal(second, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            firstFuture.get(10, TimeUnit.SECONDS);
            secondFuture.get(10, TimeUnit.SECONDS);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }

        assertThat(summaryRepository.count()).isEqualTo(2);
        assertThat(notificationDeliveryRepository.count()).isEqualTo(1);
        verifyNoInteractions(firebaseMessagingClient);
    }

    private void createAlertAfterSignal(
            InternalAlertCommand command,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시 실행 신호를 받지 못했습니다.");
            }
            internalAlertService.createAlert(command);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("테스트 스레드가 중단되었습니다.", exception);
        }
    }

    private AlertFixture createAlertFixture() {
        String plainSecret = "internal-alert-secret";
        User user = User.create(
                "internal-alert-user",
                "hashed-secret",
                CryptoUtils.sha256(plainSecret),
                UserType.USER,
                true,
                plainSecret
        );
        userRepository.save(user);

        Url url = urlRepository.save(Url.create("https://example.com/notices", "공지사항"));
        Subscription subscription = subscriptionRepository.save(
                Subscription.create(url, true, "학교", user, "previous-post"));
        FCM_Token activeToken = fcmRepository.save(
                FCM_Token.create(user, "active-token", "iPhone 15"));

        return new AlertFixture(user, subscription, activeToken);
    }

    private InternalAlertCommand createCommand(AlertFixture fixture) {
        return new InternalAlertCommand(
                fixture.user().getId(),
                fixture.subscription().getId(),
                "site-post-123",
                "새 공지",
                "https://example.com/notices/123",
                "2026-08-28T10:00:00",
                "새 공지 요약",
                true
        );
    }

    private record AlertFixture(User user, Subscription subscription, FCM_Token activeToken) {
    }
}
