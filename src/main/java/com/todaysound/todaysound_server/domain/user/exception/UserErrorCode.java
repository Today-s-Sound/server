package com.todaysound.todaysound_server.domain.user.exception;

import com.todaysound.todaysound_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404_1", "사용자를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
