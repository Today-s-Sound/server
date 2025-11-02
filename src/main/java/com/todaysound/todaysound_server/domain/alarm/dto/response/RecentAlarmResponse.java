package com.todaysound.todaysound_server.domain.alarm.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.utils.TimeUtil;

public record RecentAlarmResponse(String alias, String timeAgo, boolean isUrgent, List<SummaryResponse> summaries) {

    public static RecentAlarmResponse of(Subscription subscription) {
        return new RecentAlarmResponse(subscription.getAlias(),
                TimeUtil.toRelativeTime(subscription.getUpdatedAt()),
                subscription.isUrgent(),
                subscription.getSummaries().stream().map(SummaryResponse::of).toList());
    }

    public record SummaryResponse(Long id, String summary, LocalDateTime updatedAt) {
        public static SummaryResponse of(Summary summary) {
            return new SummaryResponse(summary.getId(), summary.getContent(),
                    summary.getUpdatedAt());
        }
    }
}
