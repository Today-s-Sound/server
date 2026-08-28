package com.todaysound.todaysound_server.domain.alarm.service;

public record InternalAlertCommand(
        Long userId,
        Long subscriptionId,
        String sitePostId,
        String title,
        String url,
        String publishedAt,
        String contentSummary,
        boolean keywordMatched
) {
}
