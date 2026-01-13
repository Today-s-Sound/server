package com.todaysound.todaysound_server.domain.alarm.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.global.application.FCMService;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAlertController implements InternalAlertApi {

    private final SubscriptionRepository subscriptionRepository;
    private final SummaryRepository summaryRepository;
    private final FCMService fcmService;

    @PostMapping("/alerts")
    public void createAlert(@RequestBody InternalAlertRequest request) {
        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND));

        // 간단한 소유자 검증 (userId 가 다르면 거부)
        if (!subscription.getUser().getId().equals(request.userId())) {
            throw BaseException.type(CommonErrorCode.FORBIDDEN);
        }

        // 알림이 활성화된 구독에 대해서만 푸시 전송
        if (subscription.isAlarmEnabled()) {
            User user = subscription.getUser();
            String prefix;

            if (request.keywordMatched == true) {
                prefix = "[" + request.siteAlias + "]";
            } else {
                prefix = "[긴급/" + request.siteAlias + "]";
            }

            fcmService.sendNotificationToUser(
                    user,
                    prefix + request.title(),
                    request.contentSummary()
            );
        }

        // sitePostId 를 해시 키로 사용
        Summary summary = Summary.create(
                request.sitePostId(),
                request.title(),
                request.contentSummary(),
                request.url(),
                request.publishedAt(),
                request.keywordMatched,
                subscription
        );

        summaryRepository.save(summary);
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


