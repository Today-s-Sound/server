package com.todaysound.todaysound_server.domain.user.dto.response;

public record FCMNotificationResponse(String message) {
    public static FCMNotificationResponse success() {
        return new FCMNotificationResponse("FCM 알림이 성공적으로 전송되었습니다.");
    }
}

