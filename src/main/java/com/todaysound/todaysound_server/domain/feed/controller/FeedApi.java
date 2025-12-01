package com.todaysound.todaysound_server.domain.feed.controller;

import com.todaysound.todaysound_server.domain.feed.dto.response.FeedResponseDTO;
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
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Tag(name = "Feed", description = "피드 조회 API")
public interface FeedApi {

    @Operation(summary = "피드 조회", description = """
            사용자의 읽지 않은 요약(Summary) 중 알람이 활성화된 구독의 요약들인 피드를
            페이지네이션하여 조회합니다.
            """, tags = {"Feed"}, operationId = "getFeeds")
    List<FeedResponseDTO> findFeeds(@ModelAttribute PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret);
}

