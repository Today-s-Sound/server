package com.todaysound.todaysound_server.domain.url.controller;

import com.todaysound.todaysound_server.domain.url.dto.response.UrlResponseDto;
import com.todaysound.todaysound_server.domain.url.service.UrlQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController implements UrlApi {

    private final UrlQueryService urlQueryService;

    @Override
    @GetMapping
    public List<UrlResponseDto> getUrls() {
        return urlQueryService.getUrls();
    }
}
