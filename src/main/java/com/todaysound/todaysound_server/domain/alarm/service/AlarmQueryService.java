package com.todaysound.todaysound_server.domain.alarm.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.domain.alarm.repository.AlarmRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmQueryService {

    private final AlarmRepository alarmRepository;

    public List<RecentAlarmResponse> getRecentAlarms(Long userId, PageRequestDTO pageRequest) {

        List<Subscription> alarms =
                alarmRepository.findSubscriptionWithUnreadSummaries(userId, pageRequest);

        return alarms.stream().map(RecentAlarmResponse::of).toList();

    }
}
