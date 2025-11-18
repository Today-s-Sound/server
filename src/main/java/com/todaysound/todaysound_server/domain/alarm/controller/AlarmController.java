package com.todaysound.todaysound_server.domain.alarm.controller;

import java.util.List;

import com.todaysound.todaysound_server.domain.alarm.dto.request.SummaryReadRequestDto;
import org.springframework.web.bind.annotation.*;
import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.dto.response.UnreadAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.service.AlarmQueryService;
import com.todaysound.todaysound_server.domain.summary.service.SummaryCommandService;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmQueryService alarmQueryService;
    private final SummaryCommandService summaryCommandService;

    // 특정 사용자의, unread summary가 있는 구독들을 대상으로 요약을 전부(읽은 것 포함) 내려주는 API
    @GetMapping()
    public List<RecentAlarmResponse> getRecentAlarms(
            @ModelAttribute final PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret) {

        return alarmQueryService.getRecentAlarms(pageRequest, userUuid, deviceSecret);
    }

    /**
     * 메인화면용 읽지 않은 알람 조회 - 읽지 않은 Summary만 포함하여 반환
     */
    @GetMapping("/unread")
    public List<UnreadAlarmResponse> getUnreadAlarmsForMain(
            @ModelAttribute final PageRequestDTO pageRequest,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret) {

        return alarmQueryService.getUnreadAlarmsForMain(pageRequest, userUuid, deviceSecret);
    }


    /**
     * Summary 읽음 처리 - 프론트에서 사용자가 Summary를 읽었을 때 호출
     */
    @PatchMapping("/summaries/read")
    public void markSummaryAsRead(@RequestBody SummaryReadRequestDto summaryReadRequestDto,
            @RequestHeader("X-User-ID") String userUuid,
            @RequestHeader("X-Device-Secret") String deviceSecret) {

        summaryCommandService.markSummaryAsRead(summaryReadRequestDto, userUuid, deviceSecret);
    }
}
