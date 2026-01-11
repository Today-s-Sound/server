package com.todaysound.todaysound_server.domain.url.service;


import com.todaysound.todaysound_server.domain.url.dto.response.UrlResponseDto;
import com.todaysound.todaysound_server.domain.url.entity.Url;
import com.todaysound.todaysound_server.domain.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlQueryService {

    private final UrlRepository urlRepository;

    public List<UrlResponseDto> getUrls() {

        List<Url> urls = urlRepository.findAll();
        return urls.stream()
                .map(url -> new UrlResponseDto(
                        url.getId(),
                        url.getLink(),
                        url.getTitle()
                ))
                .toList();
    }
}
