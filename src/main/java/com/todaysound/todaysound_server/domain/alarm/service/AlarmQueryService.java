package com.todaysound.todaysound_server.domain.alarm.service;

import java.util.List;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import org.springframework.stereotype.Service;
import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.repository.AlarmRepository;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmQueryService {

    private final AlarmRepository alarmRepository;
    private final HeaderAuthValidator headerAuthValidator;

    public List<RecentAlarmResponse> getRecentAlarms(final PageRequestDTO pageRequest, final String userUuid,
                                                     final String deviceSecret) {

        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        // 알림 활성화된 Summary 조회
        List<Summary> summaries = alarmRepository.findAlarms(user.getId(), pageRequest);

        return summaries.stream().map(RecentAlarmResponse::of).toList();
    }

}
