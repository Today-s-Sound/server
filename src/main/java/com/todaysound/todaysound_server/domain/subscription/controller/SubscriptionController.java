package com.todaysound.todaysound_server.domain.subscription.controller;

import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionCommandService;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
            @ModelAttribute final PageRequestDTO pageRequest) {

        Long userId = 1L; // TODO: 인증 로직 추가 후 수정

        return subscriptionQueryService.getMySubscriptions(pageRequest, userId);
    }

    @DeleteMapping("/{subscriptionId}")
    public void deleteSubscription(@PathVariable Long subscriptionId) {

        Long userId = 1L; // TODO: 인증 로직 추가 후 수정

        subscriptionCommandService.deleteSubscription(subscriptionId, userId);
    }

}
