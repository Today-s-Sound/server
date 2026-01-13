package com.todaysound.todaysound_server.domain.alarm.repository;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Subscription, Long>, AlarmDynamicRepository {

}
