package com.todaysound.todaysound_server.global.dto;

public record PageRequestDTO(Long cursor, Integer size) {
    private static final Integer DEFAULT_SIZE = 10;

    @Override
    public Integer size() {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        return size;
    }
}
