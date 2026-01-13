package com.todaysound.todaysound_server.domain.alarm.controller;

import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.service.AlarmQueryService;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmQueryController implements AlarmApi {

    private final AlarmQueryService alarmQueryService;

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
