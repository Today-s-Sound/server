package com.todaysound.todaysound_server.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /*
    커스텀 예외 처리
    비즈니스 로직에서 발생하는 예외처리
    ErrorCode 기반 일관성 응답 제공
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<CustomErrorResponse> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        logError("BaseException", errorCode, e);

        return convert(errorCode);
    }

    /*
    존재하지 않는 엔드포인트 또는 타입 변환 실패 처리
    잘못된 URL 요청 (404)
    파라미터 타입 변환 실패
     */
    @ExceptionHandler({NoHandlerFoundException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<CustomErrorResponse> handleNotFoundOrTypeMismatch(Exception e) {
        if (e instanceof MethodArgumentTypeMismatchException typeMismatch) {
            log.warn("Type conversion failed: {} -> {}",
                    typeMismatch.getValue(), typeMismatch.getRequiredType().getSimpleName());
        } else {
            log.warn("Handler not found: {}", e.getMessage());
        }

        return convert(CommonErrorCode.NOT_SUPPORTED_URI_ERROR);
    }

    /*
     * 지원하지 않는 HTTP 메서드 처리 (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {} for {}", e.getMethod(), e.getMessage());
        return convert(CommonErrorCode.NOT_SUPPORTED_METHOD_ERROR);
    }

    /*
     * 지원하지 않는 미디어 타입 처리 (415)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CustomErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("Media type not supported: {}", e.getContentType());
        return convert(CommonErrorCode.NOT_SUPPORTED_MEDIA_TYPE_ERROR);
    }

    /*
    예상치 못한 서버 오류 처리 (500)
    위의 핸들러들로 처리되지 않은 모든 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomErrorResponse> handleUnexpectedException(RuntimeException e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        log.error("Request info: {} {}", request.getMethod(), request.getRequestURI());

        return convert(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }


    private ResponseEntity<CustomErrorResponse> convert(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CustomErrorResponse.from(errorCode));
    }


    private void logError(String exceptionType, ErrorCode code, Exception e) {
        log.warn("[{}] {} | {} | {} | Message: {}",
                exceptionType,
                code.getStatus().value(),
                code.getErrorCode(),
                code.getMessage(),
                e.getMessage());
    }

}
