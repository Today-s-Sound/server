package com.todaysound.todaysound_server.domain.subscription.service;

import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionCreateRequest;
import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionUpdateRequest;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionCreationResponse;
import com.todaysound.todaysound_server.domain.subscription.entity.Keyword;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.exception.KeywordException;
import com.todaysound.todaysound_server.domain.subscription.exception.SubscriptionException;
import com.todaysound.todaysound_server.domain.subscription.factory.SubscriptionFactory;
import com.todaysound.todaysound_server.domain.subscription.repository.KeywordRepository;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;
import static net.logstash.logback.argument.StructuredArguments.kv;

import com.todaysound.todaysound_server.global.exception.BaseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final KeywordRepository keywordRepository;
    private final SubscriptionFactory subscriptionFactory;
    private final HeaderAuthValidator headerAuthValidator;

    public void deleteSubscription(final Long subscriptionId, final String userUuid, final String deviceSecret) {

        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> BaseException.type(SubscriptionException.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getUser().getId().equals(user.getId())) {
            throw BaseException.type(SubscriptionException.SUBSCRIPTION_NOT_PERMISSION);
        }

        subscriptionRepository.deleteById(subscriptionId);

        log.info(BUSINESS, "구독 삭제 완료 {} {}",
                kv("subscriptionId", subscriptionId),
                kv("userId", user.getId()));
    }

    public SubscriptionCreationResponse createSubscription(final String headerUserUuid,
                                                           final String headerDeviceSecret,
                                                           final SubscriptionCreateRequest requestDto) {
        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(headerUserUuid, headerDeviceSecret);

        // 구독 생성 및 저장
        Subscription subscription = subscriptionFactory.create(
                user,
                requestDto.urlId(),
                requestDto.keywordIds(),
                requestDto.alias(),
                requestDto.isAlarmEnabled()
        );
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        log.info(BUSINESS, "구독 생성 완료 {} {} {}",
                kv("subscriptionId", savedSubscription.getId()),
                kv("userId", user.getId()),
                kv("urlId", requestDto.urlId()));

        return SubscriptionCreationResponse.from(savedSubscription);
    }

    public void updateSubscription(Long subscriptionId, String userUuid, String deviceSecret,
                                   SubscriptionUpdateRequest request) {

        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> BaseException.type(SubscriptionException.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getUser().getId().equals(user.getId())) {
            throw BaseException.type(SubscriptionException.SUBSCRIPTION_NOT_PERMISSION);
        }

        if (request.keywordIds() != null) {
            List<Keyword> keywords = keywordRepository.findAllById(request.keywordIds());

            // 요청한 개수와 조회된 개수 검증
            if (keywords.size() != request.keywordIds().size()) {
                throw BaseException.type(KeywordException.KEYWORD_NOT_FOUND);
            }

            subscription.updateKeywords(keywords);
        }
        subscription.updateAlias(request.alias());
        subscription.updateIsAlarmEnabled(request.isAlarmEnabled());

        log.info(BUSINESS, "구독 수정 완료 {} {}",
                kv("subscriptionId", subscriptionId),
                kv("userId", user.getId()));
    }
}
