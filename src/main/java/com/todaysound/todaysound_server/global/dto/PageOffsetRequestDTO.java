package com.todaysound.todaysound_server.global.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record PageOffsetRequestDTO(Integer page, Integer size) {
    private static final Integer DEFAULT_SIZE = 10;
    private static final Integer DEFAULT_PAGE = 0;

    public Pageable toPageable() {
        int currentPage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int currentSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        return PageRequest.of(currentPage, currentSize);
    }

}
