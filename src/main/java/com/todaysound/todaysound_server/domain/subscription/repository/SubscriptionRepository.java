package com.todaysound.todaysound_server.domain.subscription.repository;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionDynamicRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT subscription FROM Subscription subscription WHERE subscription.id = :id")
    Optional<Subscription> findByIdForUpdate(@Param("id") Long id);

}
