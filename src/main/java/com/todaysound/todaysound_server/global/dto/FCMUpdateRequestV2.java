package com.todaysound.todaysound_server.global.dto;

import jakarta.validation.constraints.NotBlank;

public record FCMUpdateRequestV2(
        @NotBlank String fcmToken,
        @NotBlank String model
) {
}
