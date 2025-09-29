package com.todaysound.todaysound_server.domain.alarm.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.service.AlarmQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alrams")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmQueryService alarmQueryService;

    @GetMapping()
    public List<RecentAlarmResponse> getRecentAlarms(
            @ModelAttribute final PageRequestDTO pageRequest) {

        Long userId = 1L; // TODO: 인증 로직 추가 후 수정

        return alarmQueryService.getRecentAlarms(userId, pageRequest);
    }
}
