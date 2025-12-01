package com.todaysound.todaysound_server.domain.feed.dto.response;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.utils.TimeUtil;

public record HomeFeedResponse(Long subscriptionId, String alias, String summaryTitle,
        String summaryContent, String url, String timeAgo, boolean isUrgent) {

    public static HomeFeedResponse of(Summary summary) {
        return new HomeFeedResponse(summary.getSubscription().getId(),
                summary.getSubscription().getAlias(), summary.getTitle(), summary.getContent(),
                summary.getSubscription().getUrl(), TimeUtil.toRelativeTime(summary.getUpdatedAt()),
                summary.getSubscription().isUrgent());
    }

}

