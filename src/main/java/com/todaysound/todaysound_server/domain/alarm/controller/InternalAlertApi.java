package com.todaysound.todaysound_server.domain.alarm.controller;

import com.todaysound.todaysound_server.global.exception.CustomErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "InternalAlert", description = "크롤러용 알림 생성 내부 API")
public interface InternalAlertApi {

    @Operation(
            summary = "알림 생성 (크롤러용)",
            description = """
                    크롤러가 새로운 게시글을 감지했을 때 알림을 생성하기 위한 엔드포인트입니다.
                    user_id, subscription_id, site_post_id, title, url, content_raw, content_summary, keyword_matched 정보를 전달합니다.
                    """,
            tags = {"InternalAlert"},
            operationId = "createInternalAlert"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "알림 생성 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "구독 소유자 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                                "status": 403,
                                                "errorCode": "COMMON_004",
                                                "message": "접근 권한이 없습니다."
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구독을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "엔티티 없음",
                                    value = """
                                            {
                                                "status": 404,
                                                "errorCode": "COMMON_010",
                                                "message": "요청한 엔티티를 찾을 수 없습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    void createAlert(
            @RequestBody InternalAlertController.InternalAlertRequest request
    );
}


