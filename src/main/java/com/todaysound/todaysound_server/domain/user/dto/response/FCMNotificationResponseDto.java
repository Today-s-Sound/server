package com.todaysound.todaysound_server.domain.user.dto.response;

public record FCMNotificationResponseDto(String message) {
    public static FCMNotificationResponseDto success() {
        return new FCMNotificationResponseDto("FCM 알림이 성공적으로 전송되었습니다.");
    }
}

