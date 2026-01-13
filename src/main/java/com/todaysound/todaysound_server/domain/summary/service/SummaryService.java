package com.todaysound.todaysound_server.domain.summary.service;

import static com.todaysound.todaysound_server.global.utils.LogMarkers.BUSINESS;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.summary.exception.SummaryException;
import com.todaysound.todaysound_server.domain.summary.repository.SummaryRepository;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import com.todaysound.todaysound_server.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final HeaderAuthValidator headerAuthValidator;

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

