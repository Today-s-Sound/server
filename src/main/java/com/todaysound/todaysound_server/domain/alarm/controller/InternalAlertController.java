package com.todaysound.todaysound_server.domain.alarm.controller;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.todaysound.todaysound_server.domain.alarm.entity.NotificationOutbox;
import com.todaysound.todaysound_server.domain.alarm.repository.NotificationOutboxRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 크롤러용 알림 생성 엔드포인트
 *
 * [Transactional Outbox 패턴 적용]
 * - 기존: FCM 전송 → DB 저장 (순서 역전 + 원자성 없음)
 * - 개선: DB 저장(Summary + NotificationOutbox)을 단일 트랜잭션으로 묶은 후,
 *         NotificationOutboxScheduler 가 주기적으로 outbox 를 읽어 FCM 전송을 담당한다.
 * - 보장: FCM 과 DB 저장 중 하나만 성공하는 중간 상태가 발생하지 않는다.
 */
@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAlertController implements InternalAlertApi {

    private final SubscriptionRepository subscriptionRepository;
    private final SummaryRepository summaryRepository;
    private final NotificationOutboxRepository outboxRepository;

    @PostMapping("/alerts")
    @Transactional
    public void createAlert(@RequestBody InternalAlertRequest request) {
        log.info(BUSINESS, "크롤러 알림 수신 {} {} {}",
                kv("userId", request.userId()),
                kv("subscriptionId", request.subscriptionId()),
                kv("sitePostId", request.sitePostId()));

        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND));

        if (!subscription.getUser().getId().equals(request.userId())) {
            throw BaseException.type(CommonErrorCode.FORBIDDEN);
        }

        // ① Summary 저장 (트랜잭션 안)
        Summary summary = Summary.create(
                request.sitePostId(),
                request.title(),
                request.contentSummary(),
                request.url(),
                request.publishedAt(),
                request.keywordMatched(),
                subscription
        );
        summaryRepository.save(summary);

        // ② 알림이 활성화된 경우 outbox 에 기록 (같은 트랜잭션)
        // FCM 을 직접 호출하지 않고 "전송할 것"을 DB 에 기록한다.
        // Summary 저장과 outbox 저장이 하나의 트랜잭션이므로 둘 다 커밋되거나 둘 다 롤백된다.
        if (subscription.isAlarmEnabled()) {
            String title = "[" + request.siteAlias() + "] " + request.title();
            NotificationOutbox outbox = NotificationOutbox.create(
                    subscription.getUser().getId(), title, request.contentSummary());
            outboxRepository.save(outbox);
        }

        log.info(BUSINESS, "크롤러 알림 처리 완료 {} {} {}",
                kv("subscriptionId", request.subscriptionId()),
                kv("sitePostId", request.sitePostId()),
                kv("alarmQueued", subscription.isAlarmEnabled()));
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
