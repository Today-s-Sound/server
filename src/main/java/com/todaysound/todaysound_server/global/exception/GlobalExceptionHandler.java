package com.todaysound.todaysound_server.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<CustomErrorResponse> handleBaseException(BaseException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        // 비즈니스 예외는 WARN (예상된 에러)
        log.warn("Business exception occurred",
                kv("errorType", "BUSINESS"),
                kv("errorCode", errorCode.getErrorCode()),
                kv("errorMessage", errorCode.getMessage()),
                kv("exceptionClass", e.getClass().getSimpleName()),
                kv("requestUri", request.getRequestURI()),
                kv("requestMethod", request.getMethod()),
                kv("queryString", request.getQueryString()));

        return convert(errorCode);
    }

    /*
    존재하지 않는 엔드포인트 또는 타입 변환 실패 처리
    잘못된 URL 요청 (404)
    파라미터 타입 변환 실패
     */
    @ExceptionHandler({NoHandlerFoundException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<CustomErrorResponse> handleNotFoundOrTypeMismatch(Exception e, HttpServletRequest request) {
        // 클라이언트 잘못 (4xx)은 INFO로 낮춤 - 노이즈 감소
        log.info("Client error - invalid request",
                kv("errorType", "CLIENT"),
                kv("exceptionClass", e.getClass().getSimpleName()),
                kv("requestUri", request.getRequestURI()),
                kv("errorMessage", e.getMessage()));

        return convert(CommonErrorCode.NOT_SUPPORTED_URI_ERROR);
    }

    /*
     * 지원하지 않는 HTTP 메서드 처리 (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.info("Client error - method not supported",
                kv("errorType", "CLIENT"),
                kv("requestMethod", e.getMethod()),
                kv("requestUri", request.getRequestURI()));

        return convert(CommonErrorCode.NOT_SUPPORTED_METHOD_ERROR);
    }

    /*
     * 지원하지 않는 미디어 타입 처리 (415)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CustomErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.info("Client error - media type not supported",
                kv("errorType", "CLIENT"),
                kv("contentType", String.valueOf(e.getContentType())),
                kv("requestUri", request.getRequestURI()));

        return convert(CommonErrorCode.NOT_SUPPORTED_MEDIA_TYPE_ERROR);
    }

    /*
    예상치 못한 서버 오류 처리 (500)
    위의 핸들러들로 처리되지 않은 모든 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomErrorResponse> handleUnexpectedException(
            RuntimeException e, HttpServletRequest request) {

        log.error("Unexpected server error",
                kv("errorType", "UNEXPECTED"),
                kv("exceptionClass", e.getClass().getName()),
                kv("exceptionMessage", e.getMessage()),
                kv("requestUri", request.getRequestURI()),
                kv("requestMethod", request.getMethod()),
                kv("queryString", request.getQueryString()),
                kv("userAgent", request.getHeader("User-Agent")),
                kv("traceId", MDC.get("traceId")),
                e);  // 스택트레이스 포함

        return convert(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleAllException(
            Exception e, HttpServletRequest request) {

        log.error("Unhandled exception caught",
                kv("errorType", "UNHANDLED"),
                kv("exceptionClass", e.getClass().getName()),
                kv("exceptionMessage", e.getMessage()),
                kv("requestUri", request.getRequestURI()),
                kv("requestMethod", request.getMethod()),
                kv("traceId", MDC.get("traceId")),
                e);

        return convert(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<CustomErrorResponse> convert(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CustomErrorResponse.from(errorCode));
    }
}
