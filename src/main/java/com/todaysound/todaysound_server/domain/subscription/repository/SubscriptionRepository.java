package com.todaysound.todaysound_server.domain.subscription.repository;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionDynamicRepository {

//    boolean existsByUserAndUrl(User user, String url);

//    Optional<Subscription> findByUserAndUrl(User user, String url);

}
