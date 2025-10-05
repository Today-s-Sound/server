package com.todaysound.todaysound_server.domain.subscription.factory;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@Slf4j
public class SubscriptionFactory {

    /**
     * 구독 생성
     */
    public Subscription create(User user, String url) {
        log.debug("구독 생성 시작: user={}, url={}", user.getUserId(), url);

        String alias = deriveAlias(url);

        Subscription subscription = Subscription.builder()
                .user(user)
                .url(url)
                .alias(alias)
                .isUrgent(false)
                .build();

        log.debug("구독 생성 완료: subscriptionId={}, alias={}", subscription.getId(), alias);

        return subscription;
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


