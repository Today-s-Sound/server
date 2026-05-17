package com.todaysound.todaysound_server.domain.feed.repository;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import java.util.List;


public interface FeedDynamicRepository {

    List<Summary> findFeeds(Long userId, PageRequest pageRequest);

    List<Summary> findFeedsForHome(Long userId);

}
