package com.todaysound.todaysound_server.domain.subscription.repository;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.subscription.entity.SubscriptionKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionKeywordRepository extends JpaRepository<SubscriptionKeyword, Long> {

}
