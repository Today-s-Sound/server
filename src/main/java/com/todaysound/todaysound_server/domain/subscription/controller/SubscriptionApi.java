package com.todaysound.todaysound_server.domain.subscription.controller;

import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.KeywordListResponseDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionCreationResponseDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Tag(name = "Subscription", description = "사용자 구독 관리 API")
public interface SubscriptionApi {

    @Operation(
            summary = "내 구독 목록 조회",
            description = """
                    사용자의 구독을 페이지네이션하여 조회합니다.
                    X-User-ID, X-Device-Secret 헤더로 사용자를 식별하고,
                    page, size 쿼리 파라미터로 페이지 정보를 전달합니다.
                    """,
            tags = {"Subscription"},
            operationId = "getMySubscriptions"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "구독 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "구독 목록 조회 성공 예시",
                                    value = """
                                            {
                                                "errorCode": null,
                                                "message": "OK",
                                                "result": [
                                                    {
                                                        "id": 1,
                                                        "url": "https://example.com",
                                                        "alias": "예시 사이트",
                                                        "urgent": true,
                                                        "keywords": [
                                                            {
                                                                "id": 1,
                                                                "name": "장학"
                                                            }
                                                        ]
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
    List<SubscriptionResponse> getMySubscriptions(
            @ModelAttribute PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret
    );

    @Operation(
            summary = "구독 삭제",
            description = """
                    구독 ID를 이용해 사용자의 구독을 삭제합니다.
                    """,
            tags = {"Subscription"},
            operationId = "deleteSubscription"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "구독 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구독을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)
                    )
            )
    })
    void deleteSubscription(
            @PathVariable Long subscriptionId,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret
    );

    @Operation(
            summary = "구독 생성",
            description = """
                    새로운 사이트 URL과 키워드, 별칭, 긴급 여부를 기반으로 구독을 생성합니다.
                    """,
            tags = {"Subscription"},
            operationId = "createSubscription"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "구독 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "구독 생성 성공 예시",
                                    value = """
                                            {
                                                "errorCode": null,
                                                "message": "OK",
                                                "result": {
                                                    "subscriptionId": 123
                                                }
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
                            schema = @Schema(implementation = CustomErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)
                    )
            )
    })
    SubscriptionCreationResponseDto createSubscription(
            @RequestBody @Valid SubscriptionCreateRequestDto subscriptionCreateRequestDto,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret
    );

    @Operation(
            summary = "저장된 모든 키워드 목록 조회",
            description = """
                    시스템에 저장된 모든 키워드 목록을 조회합니다.
                    """,
            tags = {"Subscription"},
            operationId = "getAllKeywords"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "키워드 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = KeywordListResponseDto.class))
                    )
            )
    })
    KeywordListResponseDto getAllKeywords();

    @Operation(
            summary = "구독 알림 차단",
            description = """
                    구독 ID를 이용해 해당 구독의 알림을 차단합니다.
                    """,
            tags = {"Subscription"},
            operationId = "alarmBlock"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "구독 알림 차단 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구독을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)
                    )
            )
    })
    void alarmBlock(
            @PathVariable Long subscriptionId
    );

    @Operation(
            summary = "구독 알림 차단 해제",
            description = """
                    구독 ID를 이용해 해당 구독의 알림 차단을 해제합니다.
                    """,
            tags = {"Subscription"},
            operationId = "alarmUnBlock"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "구독 알림 차단 해제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구독을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)
                    )
            )
    })
    void alarmUnBlock(
            @PathVariable Long subscriptionId
    );
}


