package com.todaysound.todaysound_server.global.application;

import java.time.Duration;

public record FcmSendResult(
        Long referenceId,
        String attemptedToken,
        boolean success,
        boolean retryable,
        boolean unregistered,
        String messageId,
        String errorCode,
        Duration retryAfter
) {

    public static FcmSendResult success(Long referenceId, String attemptedToken, String messageId) {
        return new FcmSendResult(
                referenceId,
                attemptedToken,
                true,
                false,
                false,
                messageId,
                null,
                null
        );
    }

    public static FcmSendResult failure(
            Long referenceId,
            String attemptedToken,
            boolean retryable,
            boolean unregistered,
            String errorCode
    ) {
        return failure(referenceId, attemptedToken, retryable, unregistered, errorCode, null);
    }

    public static FcmSendResult failure(
            Long referenceId,
            String attemptedToken,
            boolean retryable,
            boolean unregistered,
            String errorCode,
            Duration retryAfter
    ) {
        return new FcmSendResult(
                referenceId,
                attemptedToken,
                false,
                retryable,
                unregistered,
                null,
                errorCode,
                retryAfter
        );
    }
}
