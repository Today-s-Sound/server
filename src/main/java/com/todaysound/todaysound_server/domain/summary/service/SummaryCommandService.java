package com.todaysound.todaysound_server.domain.summary.service;

import com.todaysound.todaysound_server.domain.alarm.dto.request.SummaryReadRequestDto;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.exception.SummaryException;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import com.todaysound.todaysound_server.global.utils.LogMarkers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SummaryCommandService {

    private final SummaryRepository summaryRepository;
    private final HeaderAuthValidator headerAuthValidator;

    /**
     * Summary를 읽음 처리 - 사용자가 Summary를 읽었을 때 호출
     */
    public void markSummaryAsRead(SummaryReadRequestDto summaryReadRequestDto, String userUuid,
            String deviceSecret) {
        List<Long> summaryIds = summaryReadRequestDto.summaryIds();

        // 핵심 비즈니스 로그 (BUSINESS 마커 사용)
        log.info(BUSINESS, "Summary 읽음 처리 시작 - userId: {}, summaryIds: {}", userUuid, summaryIds);

        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        // Summary 조회
        List<Summary> summaryList = summaryRepository.findAllById(summaryIds);

        if (summaryList.size() != summaryIds.size()) {
            log.warn("Summary 조회 실패 - 요청한 개수: {}, 조회된 개수: {}", summaryIds.size(),
                    summaryList.size());
            throw BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND);
        }

        for (Summary summary : summaryList) {
            Subscription subscription = summary.getSubscription();
            if (!subscription.getUser().getId().equals(user.getId())) {
                log.warn("Summary 접근 권한 없음 - userId: {}, summaryId: {}, ownerId: {}", user.getId(),
                        summary.getId(), subscription.getUser().getId());
                throw BaseException.type(CommonErrorCode.FORBIDDEN);
            }

            summary.markAsRead();
        }

        summaryRepository.saveAll(summaryList);

        // 핵심 비즈니스 로그 (완료)
        log.info(BUSINESS, "Summary 읽음 처리 완료 - userId: {}, 처리된 개수: {}", userUuid,
                summaryList.size());
    }

    public void deleteSummary(String UserUuid, String deviceSecret, Long summaryId) {
        // 핵심 비즈니스 로그 (BUSINESS 마커 사용)
        log.info(BUSINESS, "Summary 삭제 시작 - userId: {}, summaryId: {}", UserUuid, summaryId);

        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(UserUuid, deviceSecret);

        Summary summary = getSummaryById(summaryId);

        summaryRepository.delete(summary);

        // 핵심 비즈니스 로그 (완료)
        log.info(BUSINESS, "Summary 삭제 완료 - userId: {}, summaryId: {}", UserUuid, summaryId);
    }


    private Summary getSummaryById(Long summaryId) {
        return summaryRepository.findById(summaryId).orElseThrow(() -> {
            log.warn("Summary 조회 실패 - summaryId: {}", summaryId);
            return BaseException.type(SummaryException.SUMMARY_NOT_FOUND);
        });
    }


}

