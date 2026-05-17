package com.todaysound.todaysound_server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomErrorResponse {

    private int status;
    private String errorCode;
    private String message;

    public CustomErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.errorCode = errorCode.getErrorCode();
        this.message = errorCode.getMessage();
    }

    public static CustomErrorResponse from(ErrorCode errorCode) {
        return new CustomErrorResponse(errorCode);
    }
}
