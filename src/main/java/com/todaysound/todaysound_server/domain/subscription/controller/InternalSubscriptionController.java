package com.todaysound.todaysound_server.domain.subscription.controller;

import com.todaysound.todaysound_server.domain.subscription.dto.response.InternalSubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.repository.SubscriptionRepository;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalSubscriptionController implements InternalSubscriptionApi {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * 크롤러용: 모든 구독 정보를 단순 JSON 형태로 반환
     */
    @GetMapping("/subscriptions")
    public List<InternalSubscriptionResponse> getSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        return subscriptions.stream()
                .map(InternalSubscriptionResponse::from)
                .toList();
    }

    /**
     * 크롤러용: last_seen_post_id 업데이트
     */
    @PatchMapping("/subscriptions/{id}/last_seen")
    @Transactional
    public void updateLastSeenPostId(
            @PathVariable("id") Long id,
            @RequestBody UpdateLastSeenPostRequest request
    ) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND));

        subscription.updateLastSeenPostId(request.last_seen_post_id());
        // 트랜잭션 범위 내에서 변경 감지로 자동 반영됨
    }

    public record UpdateLastSeenPostRequest(String last_seen_post_id) {
    }
}
