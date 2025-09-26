package com.todaysound.todaysound_server.domain.subscription.controller;

import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import com.todaysound.todaysound_server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionQueryService subscriptionService;

    @GetMapping()
    public ApiResponse<List<SubscriptionResponse>> getMySubscriptions(
            @ModelAttribute final PageRequestDTO pageRequest) {

        Long userId = 1L; // TODO: 인증 로직 추가 후 수정

        return ApiResponse.success(subscriptionService.getMySubscriptions(pageRequest, userId));
    }
}
