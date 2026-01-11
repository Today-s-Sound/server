package com.todaysound.todaysound_server.domain.feed.dto.response;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.utils.TimeUtil;

public record FeedResponseDTO(
        Long subscriptionId,
        String alias,
        String summaryTitle,
        String summaryContent,
        String postUrl,
        String timeAgo,
        boolean isUrgent
) {

    public static FeedResponseDTO of(Summary summary) {
        return new FeedResponseDTO(
                summary.getSubscription().getId(),
                summary.getSubscription().getAlias(),
                summary.getTitle(),
                summary.getContent(),
                summary.getPostUrl(),
                TimeUtil.toRelativeTime(summary.getUpdatedAt()),
                summary.getSubscription().isUrgent()
        );
    }

}
