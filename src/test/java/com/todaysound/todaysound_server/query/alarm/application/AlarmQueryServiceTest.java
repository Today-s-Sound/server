package com.todaysound.todaysound_server.query.alarm.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.service.AlarmQueryService;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.url.entity.Url;
import com.todaysound.todaysound_server.domain.url.repository.UrlRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.entity.UserType;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import com.todaysound.todaysound_server.global.utils.CryptoUtils;
import com.todaysound.todaysound_server.support.ServiceTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class AlarmQueryServiceTest extends ServiceTestSupport {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private AlarmQueryService alarmQueryService;

    @Test
    void lastAlarmToggleAt_이전에_생성된_summary는_조회되지_않는다() {
        // given
        Url url = Url.create("http://example.com", "Example Site");
        urlRepository.save(url);

        String plainSecret = "plainSecret1";
        User user = User.create("userId1", "hashedSecret", CryptoUtils.sha256(plainSecret), UserType.USER, true, plainSecret);
        userRepository.save(user);

        Subscription subscription = Subscription.create(url, true, "alias", user, "lastSeenPostId");
        LocalDateTime toggleAt = LocalDateTime.now();
        ReflectionTestUtils.setField(subscription, "lastAlarmToggleAt", toggleAt);
        subscriptionRepository.save(subscription);

        // lastAlarmToggleAt 이전에 생성된 summary
        Summary summaryBefore = Summary.create("hash1", "title1", "content1", "postUrl1", "postDate1", false, subscription);
        ReflectionTestUtils.setField(summaryBefore, "createdAt", toggleAt.minusDays(1));
        ReflectionTestUtils.setField(summaryBefore, "updatedAt", toggleAt.minusDays(1));

        // lastAlarmToggleAt 이후에 생성된 summary
        Summary summaryAfter = Summary.create("hash2", "title2", "content2", "postUrl2", "postDate2", false, subscription);
        ReflectionTestUtils.setField(summaryAfter, "createdAt", toggleAt.plusDays(1));
        ReflectionTestUtils.setField(summaryAfter, "updatedAt", toggleAt.plusDays(1));

        summaryRepository.saveAll(List.of(summaryBefore, summaryAfter));

        // when
        List<RecentAlarmResponse> result = alarmQueryService.getRecentAlarms(
                new PageRequest(0, 10), user.getUserId(), plainSecret);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).alias()).isEqualTo("title2");
    }

    @Test
    void lastAlarmToggleAt이_null이면_모든_summary가_조회된다() {
        // given
        Url url = Url.create("http://example.com", "Example Site");
        urlRepository.save(url);

        String plainSecret = "plainSecret2";
        User user = User.create("userId2", "hashedSecret2", CryptoUtils.sha256(plainSecret), UserType.USER, true, plainSecret);
        userRepository.save(user);

        Subscription subscription = Subscription.create(url, true, "alias", user, "lastSeenPostId");
        // lastAlarmToggleAt은 null (기본값)
        subscriptionRepository.save(subscription);

        Summary summary1 = Summary.create("hash1", "title1", "content1", "postUrl1", "postDate1", false, subscription);
        Summary summary2 = Summary.create("hash2", "title2", "content2", "postUrl2", "postDate2", false, subscription);
        ReflectionTestUtils.setField(summary1, "createdAt", LocalDateTime.now().minusDays(10));
        ReflectionTestUtils.setField(summary1, "updatedAt", LocalDateTime.now().minusDays(10));
        ReflectionTestUtils.setField(summary2, "createdAt", LocalDateTime.now().minusDays(5));
        ReflectionTestUtils.setField(summary2, "updatedAt", LocalDateTime.now().minusDays(5));
        summaryRepository.saveAll(List.of(summary1, summary2));

        // when
        List<RecentAlarmResponse> result = alarmQueryService.getRecentAlarms(
                new PageRequest(0, 10), user.getUserId(), plainSecret);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 알람이_꺼져있는_subscription의_summary는_조회되지_않는다() {
        // given
        Url url = Url.create("http://example.com", "Example Site");
        urlRepository.save(url);

        String plainSecret = "plainSecret3";
        User user = User.create("userId3", "hashedSecret3", CryptoUtils.sha256(plainSecret), UserType.USER, true, plainSecret);
        userRepository.save(user);

        // 알람이 꺼진 구독
        Subscription subscriptionOff = Subscription.create(url, false, "aliasOff", user, "lastSeenPostId");
        subscriptionRepository.save(subscriptionOff);

        // 알람이 켜진 구독
        Subscription subscriptionOn = Subscription.create(url, true, "aliasOn", user, "lastSeenPostId");
        subscriptionRepository.save(subscriptionOn);

        Summary summaryOff = Summary.create("hash1", "titleOff", "content1", "postUrl1", "postDate1", false, subscriptionOff);
        Summary summaryOn = Summary.create("hash2", "titleOn", "content2", "postUrl2", "postDate2", false, subscriptionOn);
        summaryRepository.saveAll(List.of(summaryOff, summaryOn));

        // when
        List<RecentAlarmResponse> result = alarmQueryService.getRecentAlarms(
                new PageRequest(0, 10), user.getUserId(), plainSecret);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).alias()).isEqualTo("titleOn");
    }

}