package com.todaysound.todaysound_server.domain.alarm.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.service.AlarmQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmQueryService alarmQueryService;

    @GetMapping()
    public List<RecentAlarmResponse> getRecentAlarms(
            @ModelAttribute final PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret) {

        return alarmQueryService.getRecentAlarms(pageRequest, userUuid, deviceSecret);
    }
}
