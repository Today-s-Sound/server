package com.todaysound.todaysound_server.domain.alram.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;

public interface AlramRepository extends JpaRepository<Subscription, Long>, AlramDynamicRepository {

}
