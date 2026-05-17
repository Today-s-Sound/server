package com.todaysound.todaysound_server.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();

    String getErrorCode();

    String getMessage();
}
