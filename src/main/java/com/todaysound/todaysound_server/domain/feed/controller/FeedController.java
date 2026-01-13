package com.todaysound.todaysound_server.domain.feed.controller;

import com.todaysound.todaysound_server.domain.feed.dto.response.FeedResponse;
import com.todaysound.todaysound_server.domain.feed.dto.response.HomeFeedResponse;
import com.todaysound.todaysound_server.domain.feed.service.FeedQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController implements FeedApi {

    private final FeedQueryService feedQueryService;

    @Override
    @GetMapping()
    public List<FeedResponse> findFeeds(@ModelAttribute final PageRequest pageRequest,
                                        @RequestHeader("X-User-ID") String userUuid,
                                        @RequestHeader("X-Device-Secret") String deviceSecret) {

        return feedQueryService.findFeeds(userUuid, deviceSecret, pageRequest);
    }

    @Override
    @GetMapping("/home")
    public List<HomeFeedResponse> findFeedsForHome(@RequestHeader("X-User-ID") String userUuid,
                                                   @RequestHeader("X-Device-Secret") String deviceSecret) {

        return feedQueryService.findFeedsForHome(userUuid, deviceSecret);
    }
}
