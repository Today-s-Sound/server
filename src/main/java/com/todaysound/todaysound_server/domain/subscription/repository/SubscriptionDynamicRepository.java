package com.todaysound.todaysound_server.domain.subscription.repository;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import java.util.List;

public interface SubscriptionDynamicRepository {
    List<Subscription> findByUserId(Long userId, Integer cursor, Integer size);
}
