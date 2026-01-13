package com.todaysound.todaysound_server.domain.subscription.controller;

import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionCreateRequest;
import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionUpdateRequest;
import com.todaysound.todaysound_server.domain.subscription.dto.response.KeywordListResponse;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionCreationResponse;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionQueryService;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionService;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionApi {

    private final SubscriptionQueryService subscriptionQueryService;
    private final SubscriptionService subscriptionService;

    // 사용자의 구독을 페이지네이션(한 페이지 size 만큼)해서 가져옴.
    @GetMapping()
    public List<SubscriptionResponse> getMySubscriptions(@ModelAttribute final PageRequest pageRequest,
                                                         @RequestHeader("X-User-ID") String userUuid,
                                                         @RequestHeader("X-Device-Secret") String deviceSecret) {

        return subscriptionQueryService.getMySubscriptions(pageRequest, userUuid, deviceSecret);
    }

    @DeleteMapping("/{subscriptionId}")
    public void deleteSubscription(@PathVariable Long subscriptionId, @RequestHeader("X-User-ID") String userUuid,
                                   @RequestHeader("X-Device-Secret") String deviceSecret) {

        subscriptionService.deleteSubscription(subscriptionId, userUuid, deviceSecret);
    }

    @PostMapping()
    public SubscriptionCreationResponse createSubscription(
            @RequestBody @Valid SubscriptionCreateRequest subscriptionCreateRequest,
            @RequestHeader("X-User-ID") String userUuid, @RequestHeader("X-Device-Secret") String deviceSecret) {
        return subscriptionService.createSubscription(userUuid, deviceSecret, subscriptionCreateRequest);
    }

    /**
     * 저장된 모든 키워드 목록 조회
     */
    @GetMapping("/keywords")
    public KeywordListResponse getAllKeywords() {
        return subscriptionQueryService.getAllKeywords();
    }

    @PatchMapping("/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSubscription(@PathVariable Long subscriptionId,
                                   @RequestBody @Valid SubscriptionUpdateRequest request,
                                   @RequestHeader("X-User-ID") String userUuid,
                                   @RequestHeader("X-Device-Secret") String deviceSecret) {
        subscriptionService.updateSubscription(subscriptionId, userUuid, deviceSecret, request);
    }
}
