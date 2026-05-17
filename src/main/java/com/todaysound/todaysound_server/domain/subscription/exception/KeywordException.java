package com.todaysound.todaysound_server.domain.subscription.exception;

import com.todaysound.todaysound_server.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum KeywordException implements ErrorCode {

    KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "KEYWORD404_1", "유효하지 않은 KEYWORD ID 입니다."),
    ;

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}

