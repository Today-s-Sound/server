package com.todaysound.todaysound_server.domain.alarm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.todaysound.todaysound_server.domain.alarm.service.NotificationDeliveryService.ClaimedDelivery;
import com.todaysound.todaysound_server.global.application.FCMService;
import com.todaysound.todaysound_server.global.application.FcmSendResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class NotificationDeliveryDispatcherTest {

    @Test
    void FCM은_선점_트랜잭션이_끝난_뒤_호출한다() {
        NotificationDeliveryService deliveryService = mock(NotificationDeliveryService.class);
        FCMService fcmService = mock(FCMService.class);
        NotificationDeliveryDispatcher dispatcher = new NotificationDeliveryDispatcher(deliveryService, fcmService);
        LocalDateTime leaseUntil = LocalDateTime.of(2026, 8, 28, 10, 5);
        ClaimedDelivery claimed = new ClaimedDelivery(
                1L,
                10L,
                "token",
                "[별칭] 제목",
                "본문",
                "c".repeat(64),
                leaseUntil
        );
        when(deliveryService.claimBatch(NotificationDeliveryService.MAX_BATCH_SIZE))
                .thenReturn(List.of(claimed));
        AtomicBoolean transactionActiveDuringFcm = new AtomicBoolean(true);
        when(fcmService.sendMulticast(eq(claimed.title()), eq(claimed.body()),
                eq(claimed.eventId()), anyList()))
                .thenAnswer(invocation -> {
                    transactionActiveDuringFcm.set(
                            TransactionSynchronizationManager.isActualTransactionActive()
                    );
                    return List.of(FcmSendResult.success(
                            claimed.deliveryId(),
                            claimed.token(),
                            "message-id"
                    ));
                });

        int dispatched = dispatcher.dispatchPendingDeliveries();

        assertThat(dispatched).isEqualTo(1);
        assertThat(transactionActiveDuringFcm).isFalse();
        verify(deliveryService).applyResults(anyList());
    }

    @Test
    void 같은_메시지의_토큰은_하나의_multicast로_묶는다() {
        NotificationDeliveryService deliveryService = mock(NotificationDeliveryService.class);
        FCMService fcmService = mock(FCMService.class);
        NotificationDeliveryDispatcher dispatcher = new NotificationDeliveryDispatcher(deliveryService, fcmService);
        LocalDateTime leaseUntil = LocalDateTime.of(2026, 8, 28, 10, 5);
        String eventId = "d".repeat(64);
        List<ClaimedDelivery> claimed = List.of(
                new ClaimedDelivery(1L, 10L, "token-1", "제목", "본문", eventId, leaseUntil),
                new ClaimedDelivery(2L, 20L, "token-2", "제목", "본문", eventId, leaseUntil)
        );
        when(deliveryService.claimBatch(NotificationDeliveryService.MAX_BATCH_SIZE)).thenReturn(claimed);
        when(fcmService.sendMulticast(eq("제목"), eq("본문"), eq(eventId), anyList()))
                .thenReturn(List.of(
                        FcmSendResult.success(1L, "token-1", "message-1"),
                        FcmSendResult.failure(2L, "token-2", true, false, "UNAVAILABLE")
                ));

        dispatcher.dispatchPendingDeliveries();

        verify(fcmService).sendMulticast(eq("제목"), eq("본문"), eq(eventId), anyList());
        verify(deliveryService).applyResults(anyList());
    }

    @Test
    void FCM_호출의_RuntimeException은_영구_실패로_반영한다() {
        NotificationDeliveryService deliveryService = mock(NotificationDeliveryService.class);
        FCMService fcmService = mock(FCMService.class);
        NotificationDeliveryDispatcher dispatcher = new NotificationDeliveryDispatcher(deliveryService, fcmService);
        LocalDateTime leaseUntil = LocalDateTime.of(2026, 8, 28, 10, 5);
        ClaimedDelivery claimed = new ClaimedDelivery(
                1L,
                10L,
                "token",
                "제목",
                "본문",
                "e".repeat(64),
                leaseUntil
        );
        when(deliveryService.claimBatch(NotificationDeliveryService.MAX_BATCH_SIZE))
                .thenReturn(List.of(claimed));
        when(fcmService.sendMulticast(eq("제목"), eq("본문"), eq(claimed.eventId()), anyList()))
                .thenThrow(new IllegalArgumentException("invalid local payload"));

        dispatcher.dispatchPendingDeliveries();

        verify(deliveryService).applyResults(argThat(results -> {
            var result = results.iterator().next();
            return !result.retryable()
                    && result.attemptedToken().equals(claimed.token())
                    && result.errorCode().equals("FCM_CALL_FAILED");
        }));
    }
}
