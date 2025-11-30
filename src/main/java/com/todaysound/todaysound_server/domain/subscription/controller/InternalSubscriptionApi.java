package com.todaysound.todaysound_server.domain.subscription.controller;

import com.todaysound.todaysound_server.domain.subscription.dto.response.InternalSubscriptionResponseDto;
import com.todaysound.todaysound_server.global.exception.CustomErrorResponse;
import com.todaysound.todaysound_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "InternalSubscription", description = "크롤러용 구독 조회 및 상태 업데이트 내부 API")
public interface InternalSubscriptionApi {

    @Operation(
            summary = "모든 구독 정보 조회 (크롤러용)",
            description = """
                    크롤러에서 사용하기 위한 모든 구독 정보를 단순 JSON 형태로 반환합니다.
                    각 구독에는 사용자 ID, 사이트 URL, 별칭, 키워드, 긴급 여부, 마지막으로 본 게시글 ID가 포함됩니다.
                    """,
            tags = {"InternalSubscription"},
            operationId = "getInternalSubscriptions"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "구독 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = InternalSubscriptionResponseDto.class)),
                            examples = @ExampleObject(
                                    name = "구독 목록 예시",
                                    value = """
                                            [
                                                {
                                                    "id": 1,
                                                    "user_id": 10,
                                                    "site_url": "https://sw.dongguk.edu/board/list.do?id=S181",
                                                    "site_alias": "동국대 SW공지",
                                                    "keyword": "장학",
                                                    "urgent": true,
                                                    "last_seen_post_id": "12345"
                                                }
                                            ]
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    value = """
                                            {
                                                "status": 500,
                                                "errorCode": "SERVER_001",
                                                "message": "서버 내부 오류가 발생했습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    List<InternalSubscriptionResponseDto> getSubscriptions();

    @Operation(
            summary = "마지막으로 본 게시글 ID 업데이트 (크롤러용)",
            description = """
                    크롤러가 특정 구독에 대해 마지막으로 본 게시글 ID(last_seen_post_id)를 업데이트할 때 사용합니다.
                    """,
            tags = {"InternalSubscription"},
            operationId = "updateLastSeenPostId"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "last_seen_post_id 업데이트 성공"
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
    void updateLastSeenPostId(
            @PathVariable Long id,
            @RequestBody InternalSubscriptionController.UpdateLastSeenPostRequest request
    );
}


