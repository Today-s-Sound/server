package com.todaysound.todaysound_server.domain.alarm.repository;


import java.util.List;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.dto.PageRequest;

public interface AlarmDynamicRepository {

    List<Summary> findAlarms(Long userId, PageRequest pageRequest);

}
