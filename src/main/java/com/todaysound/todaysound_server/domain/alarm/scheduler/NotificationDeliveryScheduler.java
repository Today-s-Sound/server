package com.todaysound.todaysound_server.domain.alarm.scheduler;

import com.todaysound.todaysound_server.domain.alarm.service.NotificationDeliveryDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "notification.delivery",
        name = "scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationDeliveryScheduler {

    private final NotificationDeliveryDispatcher dispatcher;

    @Scheduled(fixedDelay = 5000)
    public void dispatchPendingDeliveries() {
        dispatcher.dispatchPendingDeliveries();
    }
}
