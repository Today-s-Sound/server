package com.todaysound.todaysound_server.domain.user.service;

import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.exception.UserErrorCode;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.todaysound.todaysound_server.global.utils.CryptoUtils;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * 고정 출력 지문(fingerprint)으로 사용자 존재 여부 확인
     */
    public boolean existsBySecretFingerprint(String deviceSecret) {
        String fingerPrint = CryptoUtils.sha256(deviceSecret);
        return userRepository.existsBySecretFingerprint(fingerPrint);
    }

    /**
     * 사용자 ID로 사용자 조회
     */
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * 고정 출력 지문(fingerprint)으로 사용자 조회
     */
    public User findBySecretFingerprint(String deviceSecret) {
        String fingerPrint = CryptoUtils.sha256(deviceSecret);
        return userRepository.findBySecretFingerprint(fingerPrint)
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));
    }

}
