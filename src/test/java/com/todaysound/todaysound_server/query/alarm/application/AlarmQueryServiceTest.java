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

//    @Test
//    void 최근_알람을_조회한다() {
//        // given
//        Url url = Url.create("http://example.com", "Example Site");
//        urlRepository.save(url);
//
//        User user = User.create("userId1", "hashedSecret", "fingerPrint", UserType.USER, true, "plainSecret");
//        userRepository.save(user);
//
//        Subscription subscription = Subscription.create(url, true, "alias", user, "lastSeenPostId");
//        subscriptionRepository.save(subscription);
//
//        Summary summary1 = Summary.create("hash1", "title1", "content1", "postUrl1", "postDate1", false, subscription);
//        Summary summary2 = Summary.create("hash2", "title2", "content2", "postUrl2", "postDate2", false, subscription);
//        Summary summary3 = Summary.create("hash3", "title3", "content3", "postUrl3", "postDate3", false, subscription);
//        Summary summary4 = Summary.create("hash4", "title4", "content4", "postUrl4", "postDate4", false, subscription);
//        ReflectionTestUtils.setField(summary4, "updatedAt", LocalDateTime.now().minusDays(1));
//        ReflectionTestUtils.setField(summary3, "updatedAt", LocalDateTime.now().minusDays(2));
//        ReflectionTestUtils.setField(summary2, "updatedAt", LocalDateTime.now().minusDays(3));
//        ReflectionTestUtils.setField(summary1, "updatedAt", LocalDateTime.now().minusDays(4));
//        summaryRepository.saveAll(List.of(summary1, summary2, summary3, summary4));
//
//        // when
//        List<RecentAlarmResponse> result = alarmQueryService.getRecentAlarms(new PageRequest(0, 5), user.getUserId(), user.getHashedSecret());
//
//        // then
//        assertThat(result.get(0).alias()).isEqualTo("title4");
//        assertThat(result.get(1).alias()).isEqualTo("title3");
//        assertThat(result.get(2).alias()).isEqualTo("title2");
//        assertThat(result.get(3).alias()).isEqualTo("title1");
//    }

}