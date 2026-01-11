package com.todaysound.todaysound_server.domain.feed.service;


import java.util.List;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import com.todaysound.todaysound_server.domain.feed.dto.response.FeedResponseDTO;
import com.todaysound.todaysound_server.domain.feed.dto.response.HomeFeedResponse;
import com.todaysound.todaysound_server.domain.feed.repository.FeedDynamicRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedQueryService {

    private final FeedDynamicRepository feedDynamicRepository;
    private final HeaderAuthValidator headerAuthValidator;

    public List<FeedResponseDTO> findFeeds(final String userUuid, final String deviceSecret,
                                           final PageRequestDTO pageRequest) {

        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        return feedDynamicRepository.findFeeds(user.getId(), pageRequest).stream()
                .map(FeedResponseDTO::of).toList();
    }

    public List<HomeFeedResponse> findFeedsForHome(final String userUuid, final String deviceSecret) {

        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        return feedDynamicRepository.findFeedsForHome(user.getId()).stream().map(HomeFeedResponse::of)
                .toList();
    }


}
