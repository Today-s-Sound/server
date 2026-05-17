package com.todaysound.todaysound_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 시크릿 관련 서비스 - 시크릿 해시화 - 시크릿 검증
 */
@Service
@RequiredArgsConstructor
public class SecretService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 시크릿 해시화
     *
     * @param rawPassword 평문 시크릿
     * @return 해시화된 시크릿
     */
    public String encode(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    /**
     * 시크릿 검증
     *
     * @param rawPassword     평문 시크릿
     * @param encodedPassword 해시화된 시크릿
     * @return 검증 결과
     */
    public boolean verify(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
