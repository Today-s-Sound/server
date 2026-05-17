package com.todaysound.todaysound_server.domain.summary.exception;

import com.todaysound.todaysound_server.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SummaryException implements ErrorCode {


    SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "SUMMARY401_1", "유효하지 않은 SUMMARY ID 입니다."),
    ;

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
