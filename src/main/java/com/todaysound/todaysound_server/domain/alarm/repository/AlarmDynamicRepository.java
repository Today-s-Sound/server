package com.todaysound.todaysound_server.domain.alarm.repository;


import java.util.List;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;

public interface AlarmDynamicRepository {
    List<Subscription> findSubscriptionWithUnreadSummaries(Long userId, PageRequestDTO pageRequest);

    public List<Summary> findUnreadSummariesAndIsAlarmEnabledByUserId(Long userId,
            PageRequestDTO pageRequest);

}
