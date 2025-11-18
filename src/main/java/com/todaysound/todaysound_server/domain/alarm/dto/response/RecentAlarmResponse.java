package com.todaysound.todaysound_server.domain.alarm.dto.response;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.utils.TimeUtil;

public record RecentAlarmResponse(Long subscriptionId, String alias, String summaryContent,
        String timeAgo, boolean isUrgent) {

    public static RecentAlarmResponse of(Summary summary) {
        return new RecentAlarmResponse(summary.getSubscription().getId(),
                summary.getSubscription().getAlias(), summary.getContent(),
                TimeUtil.toRelativeTime(summary.getUpdatedAt()),
                summary.getSubscription().isUrgent());
    }

}
