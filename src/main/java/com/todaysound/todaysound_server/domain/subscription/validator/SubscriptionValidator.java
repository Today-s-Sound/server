package com.todaysound.todaysound_server.domain.subscription.validator;

import com.todaysound.todaysound_server.domain.subscription.exception.SubscriptionException;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionValidator {

    private final SubscriptionRepository subscriptionRepository;

    public void validateCreatable(User user, String url) {
        if (url == null || url.isBlank()) {
            throw BaseException.type(SubscriptionException.SUBSCRIPTION_INVALID_URL);
        }
        // 간단 중복 체크: 동일 URL이 이미 존재하는 경우
        boolean duplicated = subscriptionRepository.existsByUserAndUrl(user, url);
        if (duplicated) {
            throw BaseException.type(SubscriptionException.SUBSCRIPTION_ALREADY_EXISTS);
        }
    }
}


