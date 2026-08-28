package com.todaysound.todaysound_server.domain.alarm.service;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.domain.alarm.repository.NotificationDeliveryRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import com.todaysound.todaysound_server.global.utils.CryptoUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalAlertService {

    private final SubscriptionRepository subscriptionRepository;
    private final SummaryRepository summaryRepository;
    private final FCMRepository fcmRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;

    @Transactional
    public void createAlert(InternalAlertCommand command) {
        Subscription subscription = subscriptionRepository.findByIdForUpdate(command.subscriptionId())
                .orElseThrow(() -> BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND));

        if (!subscription.getUser().getId().equals(command.userId())) {
            throw BaseException.type(CommonErrorCode.FORBIDDEN);
        }

        if (summaryRepository.findFirstBySubscriptionIdAndHashOrderByIdAsc(
                command.subscriptionId(), command.sitePostId()).isPresent()) {
            log.info(BUSINESS, "이미 처리한 크롤러 알림 {} {}",
                    kv("subscriptionId", command.subscriptionId()),
                    kv("sitePostId", command.sitePostId()));
            return;
        }

        Summary summary = Summary.create(
                command.sitePostId(),
                command.title(),
                command.contentSummary(),
                command.url(),
                command.publishedAt(),
                command.keywordMatched(),
                subscription
        );
        summaryRepository.save(summary);

        if (!subscription.isAlarmEnabled()) {
            return;
        }

        String eventId = CryptoUtils.sha256(
                subscription.getUrl().getId() + ":" + command.sitePostId());
        LocalDateTime now = LocalDateTime.now();
        for (FCM_Token token : fcmRepository.findByUserAndIsActiveTrue(subscription.getUser())) {
            notificationDeliveryRepository.insertPendingIfAbsent(
                    summary.getId(),
                    token.getId(),
                    eventId,
                    now
            );
        }
    }
}
