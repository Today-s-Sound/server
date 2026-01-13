package com.todaysound.todaysound_server.domain.subscription.exception;

import com.todaysound.todaysound_server.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum KeywordException implements ErrorCode{

    KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "KEYWORD404_1", "유효하지 않은 KEYWORD ID 입니다."),;

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}


//그리고 지금 종버튼에 펜 버튼을 만들어서 수정 페이지로 넘어갈 수 있게 해줄래 UI는 Create페이지랑 같지만 url은 재설정할 수 없고 별칭이랑 keyword랑 알람 받을지만 설정할 수 있어 아 일단 이 전에 현재 긴급 알람인지