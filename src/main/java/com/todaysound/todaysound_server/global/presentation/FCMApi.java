package com.todaysound.todaysound_server.global.presentation;

import com.todaysound.todaysound_server.domain.user.dto.request.FCMNotificationRequestDto;
import com.todaysound.todaysound_server.domain.user.dto.response.FCMNotificationResponseDto;
import com.todaysound.todaysound_server.global.exception.CustomErrorResponse;
import com.todaysound.todaysound_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "FCM", description = "FCM 푸시 알림 API")
public interface FCMApi {

    @Operation(summary = "FCM 푸시 알림 전송", description = """
            특정 사용자에게 FCM 푸시 알림을 전송합니다.
            클라이언트는 X-User-ID 헤더로 사용자 ID를 전달하고,
            요청 본문으로 알림 제목과 내용을 전달합니다.
            """, tags = {"FCM"}, operationId = "sendFcmNotification")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FCM 알림 전송 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "GlobalResponseAdvice로 감싸진 성공 응답", value = """
                    {
                        "errorCode": null,
                        "message": "OK",
                        "result": {
                            "message": "FCM 알림이 성공적으로 전송되었습니다."
                        }
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class), examples = @ExampleObject(name = "잘못된 요청", value = """
                    {
                        "status": 400,
                        "errorCode": "COMMON_002",
                        "message": "입력값 검증에 실패했습니다."
                    }
                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class), examples = @ExampleObject(name = "인증 실패", value = """
                    {
                        "status": 401,
                        "errorCode": "COMMON_003",
                        "message": "인증이 필요합니다."
                    }
                    """)))})
    FCMNotificationResponseDto sendNotification(@RequestHeader("X-User-ID") String userId,
                                                @Valid @RequestBody FCMNotificationRequestDto requestDto);
}


