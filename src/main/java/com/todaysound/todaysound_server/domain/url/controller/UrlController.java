package com.todaysound.todaysound_server.domain.url.controller;

import com.todaysound.todaysound_server.domain.url.dto.response.UrlResponse;
import com.todaysound.todaysound_server.domain.url.service.UrlQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController implements UrlApi {

    private final UrlQueryService urlQueryService;

    @Override
    @GetMapping
    public List<UrlResponse> getUrls() {
        return urlQueryService.getUrls();
    }
}
