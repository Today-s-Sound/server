package com.todaysound.todaysound_server.domain.subscription.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 구독 생성 요청 DTO - 헤더(X-User-ID, X-Device-Secret)는 컨트롤러의 @RequestHeader로 받고, 본 DTO는 바디의 필드만
 * 검증/바인딩합니다.
 */
public record SubscriptionCreateRequestDto(@NotNull(message = "URL ID는 필수입니다.") Long urlId,

                List<Long> keywordIds,

                String alias,

                boolean isUrgent) {
}


