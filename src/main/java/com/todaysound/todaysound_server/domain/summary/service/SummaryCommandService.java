package com.todaysound.todaysound_server.domain.summary.service;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SummaryCommandService {

    private final SummaryRepository summaryRepository;
    private final HeaderAuthValidator headerAuthValidator;

    /**
     * Summary를 읽음 처리
     * - 사용자가 Summary를 읽었을 때 호출
     */
    public void markSummaryAsRead(Long summaryId, String userUuid, String deviceSecret) {
        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        // Summary 조회
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND));

        // Summary가 속한 Subscription의 소유자 확인
        Subscription subscription = summary.getSubscription();
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw BaseException.type(CommonErrorCode.FORBIDDEN);
        }

        // 이미 읽은 경우는 그냥 성공 처리
        if (summary.isRead()) {
            return;
        }

        // 읽음 처리
        summary.markAsRead();
        summaryRepository.save(summary);
    }
}

