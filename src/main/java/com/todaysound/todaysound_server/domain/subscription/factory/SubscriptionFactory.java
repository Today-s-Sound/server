package com.todaysound.todaysound_server.domain.subscription.factory;

import com.todaysound.todaysound_server.domain.subscription.entity.Keyword;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.entity.SubscriptionKeyword;
import com.todaysound.todaysound_server.domain.subscription.repository.KeywordRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionFactory {

    private final KeywordRepository keywordRepository;

    /**
     * 구독 생성
     */
    public Subscription create(User user, String url, List<String> keywords) {
        log.debug("구독 생성 시작: user={}, url={}, keywords={}", user.getUserId(), url, keywords);

        String alias = deriveAlias(url);

        Subscription subscription = Subscription.builder()
                .user(user)
                .url(url)
                .alias(alias)
                .isUrgent(false)
                .build();

        // 키워드가 있는 경우 처리
        if (keywords != null && !keywords.isEmpty()) {
            List<SubscriptionKeyword> subscriptionKeywords = createSubscriptionKeywords(subscription, keywords);
            subscription.getSubscriptionKeywords().addAll(subscriptionKeywords);
        }

        log.debug("구독 생성 완료: subscriptionId={}, alias={}, keywordCount={}", 
                subscription.getId(), alias, 
                subscription.getSubscriptionKeywords().size());

        return subscription;
    }

    /**
     * 구독 키워드 생성 및 연결
     * - 기존 키워드가 있으면 재사용, 없으면 새로 생성
     */
    private List<SubscriptionKeyword> createSubscriptionKeywords(Subscription subscription, List<String> keywordNames) {
        List<SubscriptionKeyword> subscriptionKeywords = new ArrayList<>();

        for (String keywordName : keywordNames) {
            if (keywordName == null || keywordName.isBlank()) {
                log.warn("빈 키워드는 건너뜁니다: keywordName={}", keywordName);
                continue;
            }

            // 기존 키워드 조회 또는 생성
            Keyword keyword = keywordRepository.findByName(keywordName.trim())
                    .orElseGet(() -> {
                        Keyword newKeyword = Keyword.builder()
                                .name(keywordName.trim())
                                .build();
                        return keywordRepository.save(newKeyword);
                    });

            // SubscriptionKeyword 생성
            SubscriptionKeyword subscriptionKeyword = SubscriptionKeyword.builder()
                    .subscription(subscription)
                    .keyword(keyword)
                    .build();

            subscriptionKeywords.add(subscriptionKeyword);
        }

        return subscriptionKeywords;
    }

    /**
     * URL에서 호스트명을 추출하여 별칭 생성
     */
    private String deriveAlias(String url) {
        try {
            // 프로토콜이 없는 경우 https:// 추가
            String normalizedUrl = url;
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                normalizedUrl = "https://" + url;
            }
            
            URI uri = URI.create(normalizedUrl);
            String host = uri.getHost();
            
            // 호스트가 null인 경우 원본 URL 사용
            if (host == null || host.isEmpty()) {
                log.warn("호스트 추출 실패, 원본 URL을 별칭으로 사용: url={}", url);
                return url;
            }
            
            return host;
        } catch (Exception e) {
            log.warn("URL 파싱 실패, 원본 URL을 별칭으로 사용: url={}, error={}", url, e.getMessage());
            return url;
        }
    }
}


