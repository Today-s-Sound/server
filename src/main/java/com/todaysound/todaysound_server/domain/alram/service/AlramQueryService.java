package com.todaysound.todaysound_server.domain.alram.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.todaysound.todaysound_server.domain.alram.dto.response.RecentAlramResponse;
import com.todaysound.todaysound_server.domain.alram.repository.AlramRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.global.dto.PageOffsetRequestDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlramQueryService {

    private final AlramRepository alramRepository;

    public List<RecentAlramResponse> getRecentAlrams(Long userId,
            PageOffsetRequestDTO pageRequest) {

        List<Subscription> alrams = alramRepository.findSubscriptionWithUnreadSummaries(userId,
                pageRequest.toPageable());
        return alrams.stream().map(RecentAlramResponse::of).toList();

    }
}
