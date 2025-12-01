package com.todaysound.todaysound_server.domain.subscription.controller;

import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.KeywordListResponseDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionCreationResponseDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionCommandService;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionApi {

    private final SubscriptionQueryService subscriptionQueryService;
    private final SubscriptionCommandService subscriptionCommandService;

    // 사용자의 구독을 페이지네이션(한 페이지 size 만큼)해서 가져옴.
    @GetMapping()
    public List<SubscriptionResponse> getMySubscriptions(
            @ModelAttribute final PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret) {

        return subscriptionQueryService.getMySubscriptions(pageRequest, userUuid, deviceSecret);
    }

    @DeleteMapping("/{subscriptionId}")
    public void deleteSubscription(
            @PathVariable Long subscriptionId,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret) {

        subscriptionCommandService.deleteSubscription(subscriptionId, userUuid, deviceSecret);
    }

    @PostMapping()
    public SubscriptionCreationResponseDto createSubscription(
            @RequestBody @Valid SubscriptionCreateRequestDto subscriptionCreateRequestDto, 
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret
    ) {
        return subscriptionCommandService.createSubscription(userUuid, deviceSecret, subscriptionCreateRequestDto);
    }

    /**
     * 저장된 모든 키워드 목록 조회
     */
    @GetMapping("/keywords")
    public KeywordListResponseDto getAllKeywords() {
        return subscriptionQueryService.getAllKeywords();
    }

    @PatchMapping("/{subscriptionId}/alarm/block")
    @ResponseStatus(HttpStatus.OK)
    public void alarmBlock(
            @PathVariable Long subscriptionId
    ) {

        subscriptionCommandService.alarmBlock(subscriptionId);
    }

    @PatchMapping("/{subscriptionId}/alarm/unblock")
    @ResponseStatus(HttpStatus.OK)
    public void alarmUnBlock(
            @PathVariable Long subscriptionId
    ) {

        subscriptionCommandService.alarmUnBlock(subscriptionId);
    }
}
