package com.todaysound.todaysound_server.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSecretRequestDto(
        @NotBlank(message = "디바이스 스크릿은 필수입니다.")
        @Size(min = 8, max = 256, message = "시크릿 길이는 8~256자여야 합니다.")
        String deviceSecret
) {}
