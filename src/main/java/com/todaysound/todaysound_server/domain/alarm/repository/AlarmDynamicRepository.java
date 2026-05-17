package com.todaysound.todaysound_server.domain.alarm.repository;


import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import java.util.List;

public interface AlarmDynamicRepository {

    List<Summary> findAlarms(Long userId, PageRequest pageRequest);

}
