package com.todaysound.todaysound_server.domain.subscription.dto.response;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SubscriptionCreationResponse(
        @Schema(description = "생성된 구독 ID", example = "123")
        Long subscriptionId
) {
    public static SubscriptionCreationResponse from(Subscription subscription) {
        return new SubscriptionCreationResponse(subscription.getId());
    }
}


