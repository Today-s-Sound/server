package com.todaysound.todaysound_server.domain.subscription.repository;

import java.util.List;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;

public interface SubscriptionDynamicRepository {
  public List<Subscription> findByUserId(Long userId, Long cursor, Integer size);
}
