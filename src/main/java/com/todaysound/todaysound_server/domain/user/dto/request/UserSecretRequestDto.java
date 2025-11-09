package com.todaysound.todaysound_server.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSecretRequestDto(
        @NotBlank(message = "디바이스 스크릿은 필수입니다.")
        @Size(min = 8, max = 256, message = "시크릿 길이는 8~256자여야 합니다.")
        String deviceSecret,

        @NotBlank(message = "기종은 필수입니다.")
        @Size(min = 3, max = 50, message = "deviceName 길이는 3~256자여야 합니다.")
        String model,

        @NotBlank(message = "fcmToken은 필수입니다.")
        @Size(min = 4, max = 256, message = "fcmToken은 4~256자여야 합니다.")
        String fcmToken
) {}
