package com.todaysound.todaysound_server.domain.subscription.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.exception.SubscriptionException;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.global.exception.BaseException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;

    public void deleteSubscription(final Long subscriptionId, final Long currentUserId) {

        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> BaseException.type(SubscriptionException.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getUser().getId().equals(currentUserId)) {
            throw BaseException.type(SubscriptionException.SUBSCRIPTION_NOT_PERMISSION);
        }

        subscriptionRepository.deleteById(subscriptionId);
    }
}
