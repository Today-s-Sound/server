package com.todaysound.todaysound_server.domain.user.controller;

import com.todaysound.todaysound_server.domain.user.dto.request.FCMNotificationRequestDto;
import com.todaysound.todaysound_server.domain.user.dto.response.FCMNotificationResponseDto;
import com.todaysound.todaysound_server.domain.user.service.FCMService;
import com.todaysound.todaysound_server.domain.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM", description = "FCM 푸시 알림 API")
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FCMController {

    private final FCMService fcmService;
    private final UserQueryService userQueryService;

    @PostMapping("/send")
    public FCMNotificationResponseDto sendNotification(@RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody FCMNotificationRequestDto requestDto) {
        // X-User-ID로 사용자 조회
        var user = userQueryService.findByUserId(userId);

        // FCM 알림 전송
        fcmService.sendNotificationToUser(user, requestDto.title(), requestDto.body());

        return FCMNotificationResponseDto.success();
    }
}

