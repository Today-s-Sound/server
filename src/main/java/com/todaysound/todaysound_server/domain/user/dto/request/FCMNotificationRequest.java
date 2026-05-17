package com.todaysound.todaysound_server.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FCMNotificationRequest(
        @NotBlank(message = "알림 제목은 필수입니다.") @Size(min = 1, max = 100,
                message = "제목 길이는 1~100자여야 합니다.") String title,

        @NotBlank(message = "알림 본문은 필수입니다.") @Size(min = 1, max = 500,
                message = "본문 길이는 1~500자여야 합니다.") String body) {
}

