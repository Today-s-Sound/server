package com.todaysound.todaysound_server.domain.user.validator;

import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.domain.user.service.SecretService;
import com.todaysound.todaysound_server.global.exception.BaseException;
import com.todaysound.todaysound_server.global.exception.CommonErrorCode;
import com.todaysound.todaysound_server.global.utils.CryptoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 헤더 기반 인증 값 검증기
 * - X-User-ID, X-Device-Secret을 검증하여 유효한 User를 반환
 */
@Component
@RequiredArgsConstructor
public class HeaderAuthValidator {

    private final UserRepository userRepository;
    private final SecretService secretService;

    public User validateAndGetUser(String userUuid, String deviceSecret) {
        if (userUuid == null || userUuid.isBlank() || deviceSecret == null || deviceSecret.isBlank()) {
            throw BaseException.type(CommonErrorCode.INVALID_PARAMETER);
        }

        User user = userRepository.findByUserId(userUuid)
                .orElseThrow(() -> BaseException.type(CommonErrorCode.ENTITY_NOT_FOUND));

        // 빠른 매칭: fingerprint 비교 (SHA-256)
        String fingerprint = CryptoUtils.sha256(deviceSecret);
        if (!fingerprint.equals(user.getSecretFingerprint())) {
            // 필요시 강화: BCrypt 검증도 수행 가능
            if (!secretService.verify(deviceSecret, user.getHashedSecret())) {
                throw BaseException.type(CommonErrorCode.UNAUTHORIZED);
            }
        }

        if (!user.isActive()) {
            throw BaseException.type(CommonErrorCode.FORBIDDEN);
        }

        return user;
    }
}


