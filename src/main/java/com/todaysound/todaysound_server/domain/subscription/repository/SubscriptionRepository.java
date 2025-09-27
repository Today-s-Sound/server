package com.todaysound.todaysound_server.domain.subscription.repository;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long>, SubscriptionDynamicRepository {

}
