package com.todaysound.todaysound_server.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserType {
    ANONYMOUS("익명 사용자"),
    USER("일반 사용자"),
    ADMIN("관리자");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

}
