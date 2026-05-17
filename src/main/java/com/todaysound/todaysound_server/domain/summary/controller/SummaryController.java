package com.todaysound.todaysound_server.domain.summary.controller;

import com.todaysound.todaysound_server.domain.summary.service.SummaryService;
import com.todaysound.todaysound_server.global.exception.CustomErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Summary", description = "요약 관리 API")
@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @Operation(
            summary = "요약 삭제",
            description = """
                    요약 ID를 이용해 사용자의 요약을 삭제합니다.
                    X-User-ID, X-Device-Secret 헤더로 사용자를 인증합니다.
                    """,
            tags = {"Summary"},
            operationId = "deleteSummary"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "요약 삭제 성공"
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
                    description = "요약을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{summaryId}")
    public void deleteSummary(
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret,
            @PathVariable Long summaryId
    ) {

        summaryService.deleteSummary(userUuid, deviceSecret, summaryId);
    }
}
