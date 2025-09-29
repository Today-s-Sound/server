package com.todaysound.todaysound_server.domain.subscription.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionQueryService {

        private final SubscriptionRepository subscriptionRepository;

        public List<SubscriptionResponse> getMySubscriptions(final PageRequestDTO pageRequest,
                        final Long userId) {

                List<Subscription> mySubscriptions = subscriptionRepository.findByUserId(userId,
                                pageRequest.cursor(), pageRequest.size());

                return mySubscriptions.stream().map(subscription -> SubscriptionResponse.of(
                                subscription,
                                subscription.getSubscriptionKeywords().stream()
                                                .map(subscriptionKeyword -> subscriptionKeyword
                                                                .getKeyword())
                                                .toList()))
                                .toList();

        }
}
