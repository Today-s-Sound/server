package com.todaysound.todaysound_server.domain.subscription.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;

public record SubscriptionUpdateRequest(

        @Size(max = 10, message = "키워드는 최대 10개까지 가능합니다.") List<Long> keywordIds,

        @Size(max = 100, message = "별칭은 100자 이내여야 합니다.") String alias, Boolean isAlarmEnabled) {
}
