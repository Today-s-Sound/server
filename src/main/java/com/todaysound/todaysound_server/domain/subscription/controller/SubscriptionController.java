package com.todaysound.todaysound_server.domain.subscription.controller;

import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionCreationResponseDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionCommandService;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionQueryService subscriptionQueryService;
    private final SubscriptionCommandService subscriptionCommandService;

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

}
