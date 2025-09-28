package com.todaysound.todaysound_server.domain.user.factory;

import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.entity.UserType;
import com.todaysound.todaysound_server.domain.user.service.SecretService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.todaysound.todaysound_server.global.utils.CryptoUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserFactory {
    private final SecretService secretService;

    /**
     * 익명 사용자 생성
     */
    public User createAnonymousUser(String deviceSecret) {

        // 배포시 로깅은 제거
        log.debug("익명 사용자 생성 시작: deviceSecret={}", deviceSecret.substring(0, 8) + "...");

        // UUID 생성
        String userId = UUID.randomUUID().toString();

        // BCrypt 해쉬화
        String hashedSecret = secretService.encode(deviceSecret);

        // 중복 검사용 fingerprint 생성 (SHA-256)
        String secretFingerprint = CryptoUtils.sha256(deviceSecret);

        // User 엔티티 생성
        User user = User.builder()
                .userId(userId)
                .hashedSecret(hashedSecret)
                .secretFingerprint(secretFingerprint)
                .userType(UserType.ANONYMOUS)
                .isActive(true)
                .plainSecret(deviceSecret) // 생성 시에만 설정
                .build();

        log.debug("익명 사용자 생성 완료: userId={}", userId);

        return user;
    }


}
