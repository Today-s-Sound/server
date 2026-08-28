package com.todaysound.todaysound_server.domain.alarm.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.todaysound.todaysound_server.domain.alarm.entity.DeliveryStatus;
import com.todaysound.todaysound_server.domain.alarm.entity.NotificationDelivery;
import com.todaysound.todaysound_server.domain.alarm.repository.NotificationDeliveryRepository;
import com.todaysound.todaysound_server.domain.alarm.service.NotificationDeliveryService.ClaimedDelivery;
import com.todaysound.todaysound_server.domain.alarm.service.NotificationDeliveryService.DeliveryResult;
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
import com.todaysound.todaysound_server.support.ServiceTestSupport;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@TestPropertySource(properties = "notification.delivery.scheduler-enabled=false")
class NotificationDeliveryServiceIntegrationTest extends ServiceTestSupport {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 28, 10, 0);

    @Autowired
    private NotificationDeliveryService deliveryService;

    @Autowired
    private NotificationDeliveryRepository deliveryRepository;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private FCMRepository fcmRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Subscription subscription;
    private User user;
    private int sequence;

    @BeforeEach
    void setUp() {
        Url url = urlRepository.save(Url.create("https://example.com", "example"));
        user = userRepository.save(User.create(
                "uuid",
                "hashed-secret",
                "fingerprint",
                UserType.USER,
                true,
                "plain-secret"
        ));
        subscription = subscriptionRepository.save(
                Subscription.create(url, true, "별칭", user, "last-post")
        );
    }

    @Test
    void due_작업과_만료된_lease만_선점한다() {
        NotificationDelivery due = saveDelivery(NOW.minusMinutes(1), true);
        NotificationDelivery future = saveDelivery(NOW.plusMinutes(1), true);
        NotificationDelivery expired = saveDelivery(NOW.minusMinutes(10), true);
        expired.claim(NOW.minusSeconds(1));
        deliveryRepository.saveAndFlush(expired);

        List<ClaimedDelivery> claimed = deliveryService.claimBatch(500, NOW);

        assertThat(claimed).extracting(ClaimedDelivery::deliveryId)
                .containsExactlyInAnyOrder(due.getId(), expired.getId());
        assertThat(claimed).allSatisfy(delivery -> {
            assertThat(delivery.title()).startsWith("[별칭] ");
            assertThat(delivery.leaseUntil()).isEqualTo(NOW.plusMinutes(5));
        });
        assertThat(deliveryRepository.findById(due.getId()).orElseThrow().getAttemptCount()).isEqualTo(1);
        assertThat(deliveryRepository.findById(expired.getId()).orElseThrow().getAttemptCount()).isEqualTo(2);
        assertThat(deliveryRepository.findById(future.getId()).orElseThrow().getStatus())
                .isEqualTo(DeliveryStatus.PENDING);
    }

    @Test
    void 토큰별_부분_응답을_SENT_RETRY_FAILED로_각각_반영한다() {
        NotificationDelivery success = saveDelivery(NOW.minusMinutes(1), true);
        NotificationDelivery retry = saveDelivery(NOW.minusMinutes(1), true);
        NotificationDelivery permanentFailure = saveDelivery(NOW.minusMinutes(1), true);
        List<ClaimedDelivery> claimed = deliveryService.claimBatch(500, NOW);

        deliveryService.applyResults(List.of(
                resultFor(claimed, success, true, false, false, "message-id", null, null),
                resultFor(
                        claimed,
                        retry,
                        false,
                        true,
                        false,
                        null,
                        "UNAVAILABLE",
                        Duration.ofMinutes(3)
                ),
                resultFor(
                        claimed,
                        permanentFailure,
                        false,
                        false,
                        false,
                        null,
                        "INVALID_ARGUMENT",
                        null
                )
        ), NOW.plusSeconds(1));

        NotificationDelivery sent = deliveryRepository.findById(success.getId()).orElseThrow();
        NotificationDelivery scheduledRetry = deliveryRepository.findById(retry.getId()).orElseThrow();
        NotificationDelivery failed = deliveryRepository.findById(permanentFailure.getId()).orElseThrow();
        assertThat(sent.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(sent.getFcmMessageId()).isEqualTo("message-id");
        assertThat(scheduledRetry.getStatus()).isEqualTo(DeliveryStatus.RETRY);
        assertThat(scheduledRetry.getNextAttemptAt())
                .isEqualTo(NOW.plusMinutes(3).plusSeconds(1));
        assertThat(failed.getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    void 예전_lease의_늦은_응답은_새_선점_상태를_바꾸지_않는다() {
        NotificationDelivery delivery = saveDelivery(NOW.minusMinutes(1), true);
        ClaimedDelivery oldClaim = deliveryService.claimBatch(1, NOW).get(0);
        NotificationDelivery processing = deliveryRepository.findById(delivery.getId()).orElseThrow();
        LocalDateTime newLease = NOW.plusMinutes(6);
        processing.claim(newLease);
        deliveryRepository.saveAndFlush(processing);

        deliveryService.applyResults(List.of(new DeliveryResult(
                delivery.getId(),
                oldClaim.leaseUntil(),
                oldClaim.token(),
                true,
                false,
                false,
                "late-message",
                null,
                null
        )), NOW.plusMinutes(1));

        NotificationDelivery unchanged = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(DeliveryStatus.PROCESSING);
        assertThat(unchanged.getLeaseUntil()).isEqualTo(newLease);
        assertThat(unchanged.getFcmMessageId()).isNull();
    }

    @Test
    void 결과_반영이_잠긴_만료_lease는_동시에_재선점하지_않는다() throws Exception {
        NotificationDelivery delivery = saveDelivery(NOW.minusMinutes(1), true);
        ClaimedDelivery oldClaim = deliveryService.claimBatch(1, NOW).get(0);
        DeliveryResult oldResult = new DeliveryResult(
                delivery.getId(),
                oldClaim.leaseUntil(),
                oldClaim.token(),
                true,
                false,
                false,
                "old-message",
                null,
                null
        );
        CountDownLatch resultApplied = new CountDownLatch(1);
        CountDownLatch allowResultCommit = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            Future<?> resultFuture = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
                deliveryService.applyResults(List.of(oldResult), NOW.plusMinutes(6));
                resultApplied.countDown();
                awaitSignal(allowResultCommit, "결과 반영 트랜잭션 commit 신호를 받지 못했습니다.");
            }));
            assertThat(resultApplied.await(5, TimeUnit.SECONDS)).isTrue();

            Future<List<ClaimedDelivery>> claimFuture = executor.submit(
                    () -> deliveryService.claimBatch(1, NOW.plusMinutes(6)));

            assertThat(claimFuture.get(5, TimeUnit.SECONDS)).isEmpty();
            allowResultCommit.countDown();
            resultFuture.get(5, TimeUnit.SECONDS);
        } finally {
            allowResultCommit.countDown();
            executor.shutdownNow();
        }

        NotificationDelivery sent = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertThat(sent.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(sent.getAttemptCount()).isEqualTo(1);
        assertThat(sent.getFcmMessageId()).isEqualTo("old-message");
    }

    @Test
    void 네번째_실패는_재시도하지_않고_FAILED가_된다() {
        NotificationDelivery delivery = saveDelivery(NOW.minusMinutes(1), true);
        delivery.claim(NOW.plusMinutes(1));
        delivery.markRetry("UNAVAILABLE", NOW);
        delivery.claim(NOW.plusMinutes(2));
        delivery.markRetry("UNAVAILABLE", NOW);
        delivery.claim(NOW.plusMinutes(3));
        delivery.markRetry("UNAVAILABLE", NOW);
        delivery.claim(NOW.plusMinutes(4));
        deliveryRepository.saveAndFlush(delivery);

        deliveryService.applyResults(List.of(new DeliveryResult(
                delivery.getId(),
                delivery.getLeaseUntil(),
                delivery.getFcmToken().getFcmToken(),
                false,
                true,
                false,
                null,
                "UNAVAILABLE",
                null
        )), NOW);

        NotificationDelivery failed = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertThat(failed.getAttemptCount()).isEqualTo(NotificationDeliveryService.MAX_ATTEMPTS);
        assertThat(failed.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(failed.getNextAttemptAt()).isNull();
    }

    @Test
    void UNREGISTERED는_발송_건을_FAILED로_바꾸고_토큰을_비활성화한다() {
        NotificationDelivery delivery = saveDelivery(NOW.minusMinutes(1), true);
        ClaimedDelivery claimed = deliveryService.claimBatch(1, NOW).get(0);

        deliveryService.applyResults(List.of(new DeliveryResult(
                delivery.getId(),
                claimed.leaseUntil(),
                claimed.token(),
                false,
                false,
                true,
                null,
                "UNREGISTERED",
                null
        )), NOW.plusSeconds(1));

        assertThat(deliveryRepository.findById(delivery.getId()).orElseThrow().getStatus())
                .isEqualTo(DeliveryStatus.FAILED);
        assertThat(fcmRepository.findById(claimed.tokenId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void 이전_토큰의_UNREGISTERED_응답은_갱신된_토큰을_비활성화하지_않는다() {
        NotificationDelivery delivery = saveDelivery(NOW.minusMinutes(1), true);
        ClaimedDelivery claimed = deliveryService.claimBatch(1, NOW).get(0);
        FCM_Token refreshedToken = fcmRepository.findById(claimed.tokenId()).orElseThrow();
        refreshedToken.updateToken("refreshed-token");
        fcmRepository.saveAndFlush(refreshedToken);

        deliveryService.applyResults(List.of(new DeliveryResult(
                delivery.getId(),
                claimed.leaseUntil(),
                claimed.token(),
                false,
                false,
                true,
                null,
                "UNREGISTERED",
                null
        )), NOW.plusSeconds(1));

        NotificationDelivery failed = deliveryRepository.findById(delivery.getId()).orElseThrow();
        FCM_Token storedToken = fcmRepository.findById(claimed.tokenId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(storedToken.getFcmToken()).isEqualTo("refreshed-token");
        assertThat(storedToken.isActive()).isTrue();
        assertThat(storedToken.getInvalidatedAt()).isNull();
    }

    @Test
    void 이미_비활성인_토큰은_FCM_대상으로_반환하지_않는다() {
        NotificationDelivery delivery = saveDelivery(NOW.minusMinutes(1), false);

        List<ClaimedDelivery> claimed = deliveryService.claimBatch(1, NOW);

        assertThat(claimed).isEmpty();
        NotificationDelivery failed = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(failed.getLastErrorCode()).isEqualTo("TOKEN_INACTIVE");
    }

    private NotificationDelivery saveDelivery(LocalDateTime createdAt, boolean activeToken) {
        sequence++;
        Summary summary = summaryRepository.save(Summary.create(
                "hash-" + sequence,
                "제목 " + sequence,
                "본문 " + sequence,
                "https://example.com/posts/" + sequence,
                "2026-08-28",
                true,
                subscription
        ));
        FCM_Token token = FCM_Token.create(user, "token-" + sequence, "model");
        if (!activeToken) {
            token.deactivate(NOW.minusMinutes(1));
        }
        fcmRepository.save(token);
        NotificationDelivery delivery = NotificationDelivery.create(
                summary,
                token,
                String.format("%064x", sequence),
                createdAt
        );
        return deliveryRepository.saveAndFlush(delivery);
    }

    private DeliveryResult resultFor(
            List<ClaimedDelivery> claims,
            NotificationDelivery delivery,
            boolean success,
            boolean retryable,
            boolean unregistered,
            String messageId,
            String errorCode,
            Duration retryAfter
    ) {
        ClaimedDelivery claim = claims.stream()
                .filter(candidate -> candidate.deliveryId().equals(delivery.getId()))
                .findFirst()
                .orElseThrow();
        return new DeliveryResult(
                delivery.getId(),
                claim.leaseUntil(),
                claim.token(),
                success,
                retryable,
                unregistered,
                messageId,
                errorCode,
                retryAfter
        );
    }

    private void awaitSignal(CountDownLatch latch, String timeoutMessage) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(timeoutMessage);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("테스트 스레드가 중단되었습니다.", exception);
        }
    }
}
