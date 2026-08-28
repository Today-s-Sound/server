package com.todaysound.todaysound_server.domain.alarm.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.alarm.service.InternalAlertCommand;
import com.todaysound.todaysound_server.domain.alarm.service.InternalAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 크롤러용 알림 생성 엔드포인트
 * <p>
 * POST /internal/alerts { "user_id": 10, "subscription_id": 1, "site_post_id": "12345", "title": "게시글 제목", "url":
 * "https://...", "content_raw": "...원문...", "content_summary": "...요약...", "keyword_matched": true }
 */
@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAlertController implements InternalAlertApi {

    private final InternalAlertService internalAlertService;

    @PostMapping("/alerts")
    public void createAlert(@RequestBody InternalAlertRequest request) {
        log.info(BUSINESS, "크롤러 알림 수신 {} {} {}",
                kv("userId", request.userId()),
                kv("subscriptionId", request.subscriptionId()),
                kv("sitePostId", request.sitePostId()));

        internalAlertService.createAlert(new InternalAlertCommand(
                request.userId(),
                request.subscriptionId(),
                request.sitePostId(),
                request.title(),
                request.url(),
                request.publishedAt(),
                request.contentSummary(),
                request.keywordMatched()
        ));

        log.info(BUSINESS, "크롤러 알림 처리 완료 {} {}",
                kv("subscriptionId", request.subscriptionId()),
                kv("sitePostId", request.sitePostId()));
    }

    public record InternalAlertRequest(
            @JsonProperty("user_id") Long userId,
            @JsonProperty("subscription_id") Long subscriptionId,
            @JsonProperty("site_post_id") String sitePostId,
            @JsonProperty("site_alias") String siteAlias,
            @JsonProperty("title") String title,
            @JsonProperty("url") String url,
            @JsonProperty("published_at") String publishedAt,
            @JsonProperty("content_raw") String contentRaw,
            @JsonProperty("content_summary") String contentSummary,
            @JsonProperty("keyword_matched") boolean keywordMatched
    ) {
    }
}
