package com.todaysound.todaysound_server.domain.subscription.dto.response;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;

public record SubscriptionCreationResponseDto(
        @Schema(description = "생성된 구독 ID", example = "123")
        Long subscriptionId
) {
    public static SubscriptionCreationResponseDto from(Subscription subscription) {
        return new SubscriptionCreationResponseDto(subscription.getId());
    }
}


