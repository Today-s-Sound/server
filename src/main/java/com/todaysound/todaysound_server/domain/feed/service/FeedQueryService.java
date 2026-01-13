package com.todaysound.todaysound_server.domain.feed.service;


import com.todaysound.todaysound_server.domain.feed.dto.response.FeedResponse;
import com.todaysound.todaysound_server.domain.feed.dto.response.HomeFeedResponse;
import com.todaysound.todaysound_server.domain.feed.repository.FeedDynamicRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedQueryService {

    private final FeedDynamicRepository feedDynamicRepository;
    private final HeaderAuthValidator headerAuthValidator;

    public List<FeedResponse> findFeeds(final String userUuid, final String deviceSecret,
                                        final PageRequest pageRequest) {

        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        return feedDynamicRepository.findFeeds(user.getId(), pageRequest).stream()
                .map(FeedResponse::of).toList();
    }

    public List<HomeFeedResponse> findFeedsForHome(final String userUuid, final String deviceSecret) {

        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        return feedDynamicRepository.findFeedsForHome(user.getId()).stream().map(HomeFeedResponse::of)
                .toList();
    }


}
