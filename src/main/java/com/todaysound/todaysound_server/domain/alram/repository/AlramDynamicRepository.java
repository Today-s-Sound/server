package com.todaysound.todaysound_server.domain.alram.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;

public interface AlramDynamicRepository {
    public List<Subscription> findSubscriptionWithUnreadSummaries(Long userId, Pageable pageable);
}
