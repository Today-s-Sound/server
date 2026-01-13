package com.todaysound.todaysound_server.domain.feed.controller;

import com.todaysound.todaysound_server.domain.feed.dto.response.FeedResponse;
import com.todaysound.todaysound_server.domain.feed.dto.response.HomeFeedResponse;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Feed", description = "피드 조회 API")
public interface FeedApi {

    @Operation(summary = "피드 조회", description = """
            사용자의 읽지 않은 요약(Summary) 중 알람이 활성화된 구독의 요약들인 피드를
            페이지네이션하여 조회합니다.
            """, tags = {"Feed"}, operationId = "getFeeds")
    List<FeedResponse> findFeeds(@ModelAttribute PageRequest pageRequest,
                                 @RequestHeader("X-User-ID") String userUuid,
                                 @RequestHeader("X-Device-Secret") String deviceSecret);

    @Operation(summary = "홈화면 피드 조회", description = """
            사용자의 읽지 않은 요약(Summary) 중 알람이 활성화된 구독의 요약들인 피드를
            페이징 없이 전체 조회합니다.
            """, tags = {"Feed"}, operationId = "getFeedsForHome")
    List<HomeFeedResponse> findFeedsForHome(@RequestHeader("X-User-ID") String userUuid,
                                            @RequestHeader("X-Device-Secret") String deviceSecret);
}

