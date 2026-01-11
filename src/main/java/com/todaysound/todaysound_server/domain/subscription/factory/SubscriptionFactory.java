package com.todaysound.todaysound_server.domain.subscription.factory;

import com.todaysound.todaysound_server.domain.subscription.entity.Keyword;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.entity.SubscriptionKeyword;
import com.todaysound.todaysound_server.domain.subscription.repository.KeywordRepository;
import com.todaysound.todaysound_server.domain.url.entity.Url;
import com.todaysound.todaysound_server.domain.url.repository.UrlRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionFactory {

    private final KeywordRepository keywordRepository;
    private final UrlRepository urlRepository;

    /**
     * 구독 생성
     */
    public Subscription create(User user, Long urlId, List<Long> keywordIds, String alias,
            boolean isUrgent) {
        log.debug("구독 생성 시작: user={}, urlId={}, keywordIds={}", user.getUserId(), urlId,
                keywordIds);

        // URL 엔티티 조회
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND));

        Subscription subscription =
                Subscription.builder().user(user).url(url).alias(alias).isUrgent(isUrgent).build();

        // 키워드가 있는 경우 처리
        if (keywordIds != null && !keywordIds.isEmpty()) {
            List<SubscriptionKeyword> subscriptionKeywords =
                    createSubscriptionKeywordsFromIds(subscription, keywordIds);
            subscription.getSubscriptionKeywords().addAll(subscriptionKeywords);
        }

        log.debug("구독 생성 완료: subscriptionId={}, alias={}, keywordCount={}", subscription.getId(),
                alias, subscription.getSubscriptionKeywords().size());

        return subscription;
    }

    /**
     * 키워드 ID 리스트로 SubscriptionKeyword 생성
     */
    private List<SubscriptionKeyword> createSubscriptionKeywordsFromIds(Subscription subscription,
            List<Long> keywordIds) {
        // 키워드 ID로 키워드 조회
        List<Keyword> keywords = keywordRepository.findAllById(keywordIds);

        // SubscriptionKeyword 생성
        List<SubscriptionKeyword> subscriptionKeywords = new ArrayList<>();
        for (Keyword keyword : keywords) {
            SubscriptionKeyword subscriptionKeyword = SubscriptionKeyword.builder()
                    .subscription(subscription).keyword(keyword).build();
            subscriptionKeywords.add(subscriptionKeyword);
        }

        return subscriptionKeywords;
    }
}


