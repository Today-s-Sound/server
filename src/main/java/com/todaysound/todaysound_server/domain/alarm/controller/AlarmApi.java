package com.todaysound.todaysound_server.domain.alarm.controller;

import com.todaysound.todaysound_server.domain.alarm.dto.request.SummaryReadRequestDto;
import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.dto.response.UnreadAlarmResponse;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import com.todaysound.todaysound_server.global.exception.CustomErrorResponse;
import com.todaysound.todaysound_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
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
                                                        "timeAgo": "5분 전",
                                                        "urgent": true
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
            @ModelAttribute PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret
    );

    @Operation(
            summary = "읽지 않은 알람 조회 (메인 화면용)",
            description = """
                    메인 화면에서 사용할 읽지 않은 알람 목록을 조회합니다.
                    각 구독별로 읽지 않은 Summary만 포함하여 반환합니다.
                    """,
            tags = {"Alarm"},
            operationId = "getUnreadAlarmsForMain"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "읽지 않은 알람 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "읽지 않은 알람 목록 예시",
                                    value = """
                                            {
                                                "errorCode": null,
                                                "message": "OK",
                                                "result": [
                                                    {
                                                        "subscriptionId": 1,
                                                        "alias": "동국대 SW 융합교육원",
                                                        "url": "https://example.com",
                                                        "timeAgo": "10분 전",
                                                        "urgent": false,
                                                        "unreadCount": 3,
                                                        "unreadSummaries": [
                                                            {
                                                                "id": 1,
                                                                "content": "요약 내용...",
                                                                "updatedAt": "2025-01-01T12:00:00"
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            )
    })
    List<UnreadAlarmResponse> getUnreadAlarmsForMain(
            @ModelAttribute PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret
    );

    @Operation(
            summary = "요약 읽음 처리",
            description = """
                    사용자가 요약(Summary)을 읽었을 때 해당 요약들을 읽음 처리합니다.
                    summaryIds 목록을 전달하면 해당 ID들의 요약이 읽음 처리됩니다.
                    """,
            tags = {"Alarm"},
            operationId = "markSummaryAsRead"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "요약 읽음 처리 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)
                    )
            )
    })
    void markSummaryAsRead(
            @RequestBody SummaryReadRequestDto summaryReadRequestDto,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret
    );
}


