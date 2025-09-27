package com.todaysound.todaysound_server.domain.alram.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.todaysound.todaysound_server.domain.alram.dto.response.RecentAlramResponse;
import com.todaysound.todaysound_server.domain.alram.service.AlramQueryService;
import com.todaysound.todaysound_server.global.dto.PageCursorRequestDTO;
import com.todaysound.todaysound_server.global.dto.PageOffsetRequestDTO;
import com.todaysound.todaysound_server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alrams")
@RequiredArgsConstructor
public class AlramController {

    private final AlramQueryService alramQueryService;

    @GetMapping()
    public ApiResponse<List<RecentAlramResponse>> getRecentAlrams(
            @ModelAttribute final PageOffsetRequestDTO pageRequest) {

        Long userId = 1L; // TODO: 인증 로직 추가 후 수정

        return ApiResponse.success(alramQueryService.getRecentAlrams(userId, pageRequest));
    }
}
