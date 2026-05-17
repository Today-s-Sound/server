package com.todaysound.todaysound_server.command.summary.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.infra.scheduler.SummaryCleanupScheduler;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.url.entity.Url;
import com.todaysound.todaysound_server.domain.url.repository.UrlRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.entity.UserType;
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

}