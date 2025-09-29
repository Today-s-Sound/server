package com.todaysound.todaysound_server.domain.subscription.exception;

import org.springframework.http.HttpStatus;
import com.todaysound.todaysound_server.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionException implements ErrorCode {

    SUBSCRIPTION_NOT_PERMISSION(HttpStatus.UNAUTHORIZED, "SUBSCRIPTION401_1",
            "해당 구독을 삭제할 권한이 없습니다."), SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND,
                    "SUBSCRIPTION404_1", "해당 구독을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}

