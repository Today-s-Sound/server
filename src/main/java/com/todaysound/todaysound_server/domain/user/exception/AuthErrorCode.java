package com.todaysound.todaysound_server.domain.user.exception;

import com.todaysound.todaysound_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {

    DEVICE_SECRET_ALREADY_EXISTED(HttpStatus.BAD_REQUEST, "AUTH409_1",
            "이미 존재하는 디바이스 시크릿입니다."), FCM_TOKEN_ALREADY_EXISTED(HttpStatus.BAD_REQUEST, "AUTH409_2",
            "이미 존재하는 FCM 토큰입니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;

}
