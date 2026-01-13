package com.todaysound.todaysound_server.domain.alarm.controller;

import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import com.todaysound.todaysound_server.global.exception.CustomErrorResponse;
import com.todaysound.todaysound_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Tag(name = "Alarm", description = "알람 조회 및 읽음 처리 API")
public interface AlarmApi {

    @Operation(
            summary = "최근 알람 조회",
            description = """
                    특정 사용자의, 읽지 않은 요약(Summary)이 있는 구독들을 대상으로
                    모든 요약(읽은 것 포함)을 페이지네이션하여 반환합니다.
                    """,
            tags = {"Alarm"},
            operationId = "getRecentAlarms"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "최근 알람 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "최근 알람 목록 예시",
                                    value = """
                                            {
                                                "errorCode": null,
                                                "message": "OK",
                                                "result": [
                                                    {
                                                        "subscriptionId": 1,
                                                        "alias": "동국대 SW 융합교육원",
                                                        "summaryContent": "요약된 알림 내용...",
                                                        "timeAgo": "5분 전"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "잘못된 요청",
                                    value = """
                                            {
                                                "status": 400,
                                                "errorCode": "COMMON_002",
                                                "message": "입력값 검증에 실패했습니다."
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                                "status": 401,
                                                "errorCode": "COMMON_003",
                                                "message": "인증이 필요합니다."
                                            }
                                            """
                            )
                    )
            )
    })
    List<RecentAlarmResponse> getRecentAlarms(
            @ModelAttribute PageRequest pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret
    );

}


