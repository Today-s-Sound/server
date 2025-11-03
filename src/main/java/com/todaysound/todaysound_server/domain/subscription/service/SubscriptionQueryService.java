package com.todaysound.todaysound_server.domain.subscription.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.todaysound.todaysound_server.domain.subscription.dto.response.KeywordListResponseDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.entity.Keyword;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.KeywordRepository;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionQueryService {

        private final SubscriptionRepository subscriptionRepository;
        private final KeywordRepository keywordRepository;
        private final HeaderAuthValidator headerAuthValidator;

        public List<SubscriptionResponse> getMySubscriptions(final PageRequestDTO pageRequest,
                        final String userUuid, final String deviceSecret) {

                // 헤더 인증 검증 및 사용자 획득
                User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

                List<Subscription> mySubscriptions = subscriptionRepository.findByUserId(user.getId(),
                                pageRequest.page(), pageRequest.size());

                return mySubscriptions.stream().map(subscription -> SubscriptionResponse.of(
                                subscription,
                                subscription.getSubscriptionKeywords().stream()
                                                .map(subscriptionKeyword -> subscriptionKeyword
                                                                .getKeyword())
                                                .toList()))
                                .toList();

        }

        /**
         * 저장된 모든 키워드 목록 조회
         */
        public KeywordListResponseDto getAllKeywords() {
                List<Keyword> keywords = keywordRepository.findAll();
                return KeywordListResponseDto.from(keywords);
        }
}
