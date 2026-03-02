package com.todaysound.todaysound_server.domain.alarm.repository;

import com.todaysound.todaysound_server.domain.alarm.entity.NotificationOutbox;
import com.todaysound.todaysound_server.domain.alarm.entity.OutboxStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
