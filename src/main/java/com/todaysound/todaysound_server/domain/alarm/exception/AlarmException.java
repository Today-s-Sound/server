package com.todaysound.todaysound_server.domain.alarm.exception;

import com.todaysound.todaysound_server.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AlarmException implements ErrorCode {

    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "ALARM401_1", "유효하지 않은 ALARM ID 입니다."),;

    private final HttpStatus status;
    private final String errorCode;
    private final String message;

}
