package com.todaysound.todaysound_server.domain.feed.dto.response;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.utils.TimeUtil;

public record FeedResponseDTO(Long subscriptionId, String alias, String summaryContent, String url,
        String timeAgo, boolean isUrgent) {


    public static FeedResponseDTO of(Summary summary) {
        return new FeedResponseDTO(summary.getSubscription().getId(),
                summary.getSubscription().getAlias(), summary.getContent(),
                summary.getSubscription().getUrl(), TimeUtil.toRelativeTime(summary.getUpdatedAt()),
                summary.getSubscription().isUrgent());
    }


}
