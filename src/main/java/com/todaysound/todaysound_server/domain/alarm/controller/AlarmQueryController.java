package com.todaysound.todaysound_server.domain.alarm.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.service.AlarmQueryService;
import com.todaysound.todaysound_server.domain.summary.service.SummaryCommandService;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmQueryController implements AlarmApi {

    private final AlarmQueryService alarmQueryService;
    private final SummaryCommandService summaryCommandService;

    /**
     * 최근 알림 목록 조회
     */
    @GetMapping()
    @Override
    public List<RecentAlarmResponse> getRecentAlarms(
            @ModelAttribute final PageRequest pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret) {

        return alarmQueryService.getRecentAlarms(pageRequest, userUuid, deviceSecret);
    }

}
