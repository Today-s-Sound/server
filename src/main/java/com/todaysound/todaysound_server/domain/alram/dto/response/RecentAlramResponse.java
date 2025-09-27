package com.todaysound.todaysound_server.domain.alram.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.utils.TimeUtil;

public record RecentAlramResponse(String alias, String timeAgo, List<SummaryResponse> summaries) {

    public static RecentAlramResponse of(Subscription subscription) {
        return new RecentAlramResponse(subscription.getAlias(),
                TimeUtil.toRelativeTime(subscription.getUpdatedAt()),
                subscription.getSummaries().stream().map(SummaryResponse::of).toList());
    }

    public record SummaryResponse(Long id, String summary, LocalDateTime updatedAt) {
        public static SummaryResponse of(Summary summary) {
            return new SummaryResponse(summary.getId(), summary.getContent(),
                    summary.getUpdatedAt());
        }
    }
}
