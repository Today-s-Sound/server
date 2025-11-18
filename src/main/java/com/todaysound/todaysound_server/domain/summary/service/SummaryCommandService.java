package com.todaysound.todaysound_server.domain.summary.service;

import com.todaysound.todaysound_server.domain.alarm.dto.request.SummaryReadRequestDto;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SummaryCommandService {

    private final SummaryRepository summaryRepository;
    private final HeaderAuthValidator headerAuthValidator;

    /**
     * Summary를 읽음 처리 - 사용자가 Summary를 읽었을 때 호출
     */
    public void markSummaryAsRead(SummaryReadRequestDto summaryReadRequestDto, String userUuid, String deviceSecret) {

        List<Long> summaryIds = summaryReadRequestDto.summaryIds();

        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        // Summary 조회
        List<Summary> summaryList = summaryRepository.findAllById(summaryIds);

        if (summaryList.size() != summaryIds.size()) {
            throw BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND);
        }

        for (Summary summary : summaryList) {
            Subscription subscription = summary.getSubscription();
            if (!subscription.getUser().getId().equals(user.getId())) {
                throw BaseException.type(CommonErrorCode.FORBIDDEN);
            }

            summary.markAsRead();
        }

        summaryRepository.saveAll(summaryList);
    }
}

