package com.todaysound.todaysound_server.global.application;

import java.util.Objects;

public record FcmTarget(Long referenceId, String token) {

    public FcmTarget {
        Objects.requireNonNull(referenceId, "referenceId must not be null");
        Objects.requireNonNull(token, "token must not be null");
    }
}
