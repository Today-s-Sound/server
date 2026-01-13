package com.todaysound.todaysound_server.domain.url.service;


import com.todaysound.todaysound_server.domain.url.dto.response.UrlResponse;
import com.todaysound.todaysound_server.domain.url.entity.Url;
import com.todaysound.todaysound_server.domain.url.repository.UrlRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlQueryService {

    private final UrlRepository urlRepository;

    public List<UrlResponse> getUrls() {

        List<Url> urls = urlRepository.findAll();
        return urls.stream()
                .map(url -> new UrlResponse(
                        url.getId(),
                        url.getLink(),
                        url.getTitle()
                ))
                .toList();
    }
}
