package com.todaysound.todaysound_server.domain.alarm.dto.response;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.utils.TimeUtil;

public record RecentAlarmResponse(
        Long subscriptionId,
        Long summaryId,
        String alias,
        String summaryContent,
        String postUrl,
        String timeAgo,
        boolean isUrgent,
        boolean isRead
) {

    public static RecentAlarmResponse of(Summary summary) {

        return new RecentAlarmResponse(
                summary.getSubscription().getId(),
                summary.getId(),
                summary.getTitle(),
                summary.getContent(),
                summary.getPostUrl(),
                TimeUtil.toRelativeTime(summary.getUpdatedAt()),
                summary.getSubscription().isUrgent(),
                summary.isRead()

        );
    }

}
