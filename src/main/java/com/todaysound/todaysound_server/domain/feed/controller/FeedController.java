package com.todaysound.todaysound_server.domain.feed.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.todaysound.todaysound_server.domain.feed.dto.response.FeedResponseDTO;
import com.todaysound.todaysound_server.domain.feed.service.FeedQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController implements FeedApi {

    private final FeedQueryService feedQueryService;

    @Override
    @GetMapping()
    public List<FeedResponseDTO> findFeeds(@ModelAttribute final PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret) {

        return feedQueryService.findFeeds(userUuid, deviceSecret, pageRequest);
    }
}
