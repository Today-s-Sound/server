package com.todaysound.todaysound_server.domain.alarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;

public interface AlarmRepository extends JpaRepository<Subscription, Long>, AlarmDynamicRepository {

}
