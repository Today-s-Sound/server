package com.todaysound.todaysound_server.domain.subscription.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.exception.SubscriptionException;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionCreationResponseDto;
import com.todaysound.todaysound_server.domain.subscription.factory.SubscriptionFactory;
import com.todaysound.todaysound_server.domain.subscription.validator.SubscriptionValidator;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import com.todaysound.todaysound_server.global.exception.BaseException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionFactory subscriptionFactory;
    private final SubscriptionValidator subscriptionValidator;
    private final HeaderAuthValidator headerAuthValidator;

    public void deleteSubscription(final Long subscriptionId, final String userUuid, final String deviceSecret) {

        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> BaseException.type(SubscriptionException.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getUser().getId().equals(user.getId())) {
            throw BaseException.type(SubscriptionException.SUBSCRIPTION_NOT_PERMISSION);
        }

        subscriptionRepository.deleteById(subscriptionId);
    }

    public SubscriptionCreationResponseDto createSubscription(final String headerUserUuid,
                                                             final String headerDeviceSecret,
                                                             final SubscriptionCreateRequestDto requestDto) {
        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(headerUserUuid, headerDeviceSecret);

        // 도메인 검증 (형식)
        subscriptionValidator.validateCreatable(user, requestDto.url());

        // 중복 체크: 동일 URL이 이미 존재하면 기존 구독 반환 (저장하지 않음)
        return subscriptionRepository.findByUserAndUrl(user, requestDto.url())
                .map(SubscriptionCreationResponseDto::from)
                .orElseGet(() -> {
                    // 중복이 아닌 경우에만 새로 생성 및 저장
                    Subscription subscription = subscriptionFactory.create(user, requestDto.url(), requestDto.keywords(), requestDto.alias(), requestDto.isUrgent());
                    Subscription savedSubscription = subscriptionRepository.save(subscription);
                    return SubscriptionCreationResponseDto.from(savedSubscription);
                });
    }
}
