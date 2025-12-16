package com.todaysound.todaysound_server.domain.alarm.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.utils.TimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메인화면용 읽지 않은 알람 응답 DTO
 * - 읽지 않은 Summary만 포함
 */
public record UnreadAlarmResponse(
        @Schema(description = "구독 ID", example = "1")
        Long subscriptionId,
        @Schema(description = "구독 별칭", example = "example.com")
        String alias,
        @Schema(description = "구독 URL", example = "https://example.com")
        String url,
        @Schema(description = "상대 시간", example = "5분 전")
        String timeAgo,
        @Schema(description = "긴급 여부", example = "true")
        boolean isUrgent,
        @Schema(description = "읽지 않은 요약 개수", example = "3")
        int unreadCount,
        @Schema(description = "읽지 않은 요약 목록")
        List<UnreadSummaryResponse> unreadSummaries
) {
    public static UnreadAlarmResponse of(Subscription subscription) {
        // 읽지 않은 Summary만 필터링
        List<Summary> unreadSummaries = subscription.getSummaries().stream()
                .filter(summary -> !summary.isRead())
                .sorted((s1, s2) -> s2.getUpdatedAt().compareTo(s1.getUpdatedAt())) // 최신순 정렬
                .toList();

        return new UnreadAlarmResponse(
                subscription.getId(),
                subscription.getAlias(),
                subscription.getUrl().getLink(),
                TimeUtil.toRelativeTime(subscription.getUpdatedAt()),
                subscription.isUrgent(),
                unreadSummaries.size(),
                unreadSummaries.stream().map(UnreadSummaryResponse::of).toList()
        );
    }

    public record UnreadSummaryResponse(
            @Schema(description = "요약 ID", example = "1")
            Long id,
            @Schema(description = "원본 게시글 URL", example = "https://example.com/post/1")
            String postUrl,
            @Schema(description = "요약 내용", example = "요약 내용...")
            String content,
            @Schema(description = "업데이트 시각")
            LocalDateTime updatedAt
    ) {
        public static UnreadSummaryResponse of(Summary summary) {
            return new UnreadSummaryResponse(
                    summary.getId(),
                    summary.getPostUrl(),
                    summary.getContent(),
                    summary.getUpdatedAt()
            );
        }
    }
}

