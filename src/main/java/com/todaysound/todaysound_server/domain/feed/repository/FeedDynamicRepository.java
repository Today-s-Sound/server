package com.todaysound.todaysound_server.domain.feed.repository;

import java.util.List;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.dto.PageRequest;



public interface FeedDynamicRepository {

    List<Summary> findFeeds(Long userId, PageRequest pageRequest);

    List<Summary> findFeedsForHome(Long userId);

}
