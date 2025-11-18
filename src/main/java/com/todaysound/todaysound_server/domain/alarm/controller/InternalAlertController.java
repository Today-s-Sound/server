package com.todaysound.todaysound_server.domain.alarm.controller;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 크롤러용 알림 생성 엔드포인트
 *
 * POST /internal/alerts
 * {
 *   "user_id": 10,
 *   "subscription_id": 1,
 *   "site_post_id": "12345",
 *   "title": "게시글 제목",
 *   "url": "https://...",
 *   "content_raw": "...원문...",
 *   "content_summary": "...요약...",
 *   "is_urgent": true
 * }
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAlertController {

    private final SubscriptionRepository subscriptionRepository;
    private final SummaryRepository summaryRepository;

    @PostMapping("/alerts")
    public void createAlert(@RequestBody InternalAlertRequest request) {
        Subscription subscription = subscriptionRepository.findById(request.subscription_id())
                .orElseThrow(() -> BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND));

        // 간단한 소유자 검증 (user_id 가 다르면 거부)
        if (!subscription.getUser().getId().equals(request.user_id())) {
            throw BaseException.type(CommonErrorCode.FORBIDDEN);
        }

        // site_post_id 를 해시 키로 사용
        Summary summary = Summary.create(
                request.site_post_id(),
                request.content_summary(),
                subscription
        );

        summaryRepository.save(summary);
    }

    public record InternalAlertRequest(
            Long user_id,
            Long subscription_id,
            String site_post_id,
            String title,
            String url,
            String content_raw,
            String content_summary,
            boolean is_urgent
    ) {
    }
}


