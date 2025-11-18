package com.todaysound.todaysound_server.domain.subscription.dto.response;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.entity.SubscriptionKeyword;

import java.util.List;

/**
 * 크롤러 전용 구독 응답 DTO
 *
 * [
 *   {
 *     "id": 1,
 *     "user_id": 10,
 *     "site_url": "https://sw.dongguk.edu/board/list.do?id=S181",
 *     "site_alias": "동국대 SW공지",
 *     "keyword": "장학",
 *     "urgent": true,
 *     "last_seen_post_id": "12345"
 *   }
 * ]
 */
public record InternalSubscriptionResponseDto(
        Long id,
        Long user_id,
        String site_url,
        String site_alias,
        String keyword,
        boolean urgent,
        String last_seen_post_id
) {

    public static InternalSubscriptionResponseDto from(Subscription subscription) {
        String keyword = extractFirstKeyword(subscription.getSubscriptionKeywords());

        // 빈 문자열은 "아직 본 적 없음" 이므로 null 로 내려서 크롤러에서 None 으로 처리되게 함
        String lastSeenPostId = subscription.getLastSeenPostId();
        if (lastSeenPostId != null && lastSeenPostId.isBlank()) {
            lastSeenPostId = null;
        }

        return new InternalSubscriptionResponseDto(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getUrl(),
                subscription.getAlias(),
                keyword,
                subscription.isUrgent(),
                lastSeenPostId
        );
    }

    private static String extractFirstKeyword(List<SubscriptionKeyword> subscriptionKeywords) {
        if (subscriptionKeywords == null || subscriptionKeywords.isEmpty()) {
            return null;
        }
        return subscriptionKeywords.get(0).getKeyword().getName();
    }
}


