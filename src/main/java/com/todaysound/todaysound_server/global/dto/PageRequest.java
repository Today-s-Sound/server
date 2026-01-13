package com.todaysound.todaysound_server.global.dto;

public record PageRequest(Long page, Integer size) {
    private static final Integer DEFAULT_SIZE = 5;

    @Override
    public Integer size() {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        return size;
    }
}
