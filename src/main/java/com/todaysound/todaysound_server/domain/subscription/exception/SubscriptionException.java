package com.todaysound.todaysound_server.domain.subscription.exception;

import com.todaysound.todaysound_server.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SubscriptionException implements ErrorCode {

    SUBSCRIPTION_NOT_PERMISSION(HttpStatus.UNAUTHORIZED, "SUBSCRIPTION401_1", "해당 구독을 삭제할 권한이 없습니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION404_1", "해당 구독을 찾을 수 없습니다."),
    SUBSCRIPTION_INVALID_URL(HttpStatus.BAD_REQUEST, "SUBSCRIPTION400_1", "유효하지 않은 URL입니다."),
    SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "SUBSCRIPTION409_1", "이미 존재하는 구독 URL입니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}

