package com.todaysound.todaysound_server.global.response;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.todaysound.todaysound_server.global.exception.CustomErrorResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"errorCode", "message", "result"})
public class ApiResponse<T> {

    private String errorCode; // 에러 코드
    private String message; // 응답 메시지
    private T result; // 실제 데이터(제네릭)

    // 성공 응답
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(null, "OK", result);
    }

    //실패응답 통일시 사용
    public static <T> ApiResponse<T> fail(CustomErrorResponse customErrorResponse) {
        return new ApiResponse<>(customErrorResponse.getErrorCode(), customErrorResponse.getMessage(), null);
    }

}