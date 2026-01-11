package com.todaysound.todaysound_server.domain.subscription.dto.response;

import com.todaysound.todaysound_server.domain.subscription.entity.Keyword;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;

import java.util.List;

public record SubscriptionResponse(

        Long id, String url, String alias, boolean isAlarmEnabled, List<KeywordResponse> keywords

) {

    public static SubscriptionResponse of(Subscription subscription, List<Keyword> keywords) {

        return new SubscriptionResponse(subscription.getId(), subscription.getUrl().getLink(),
                subscription.getAlias(),
                subscription.isAlarmEnabled(),
                keywords.stream().map(KeywordResponse::of).toList());
    }

    public record KeywordResponse(Long id, String name) {
        public static KeywordResponse of(final Keyword keyword) {
            return new KeywordResponse(keyword.getId(), keyword.getName());
        }
    }
}
