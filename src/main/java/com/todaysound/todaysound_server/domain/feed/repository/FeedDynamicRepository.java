package com.todaysound.todaysound_server.domain.feed.repository;

import java.util.List;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;


public interface FeedDynamicRepository {

    public List<Summary> findUnreadSummariesByUserId(Long userId, PageRequestDTO pageRequest);

}
