package com.todaysound.todaysound_server.domain.user.service;

import com.todaysound.todaysound_server.domain.user.dto.request.UserSecretRequestDto;
import com.todaysound.todaysound_server.domain.user.dto.response.UserIdResponseDto;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.factory.UserFactory;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.domain.user.validator.UserValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserFactory userFactory;

    public UserIdResponseDto anonymous(UserSecretRequestDto userSecretRequestDto) {

        // 중복 시크릿이 있나 없나 검증
        userValidator.validateUniqueSecret(userSecretRequestDto.deviceSecret());

        // 익명 사용자 생성
        User user = userFactory.createAnonymousUser(userSecretRequestDto.deviceSecret());

        // 데이터베이스에 저장
        User savedUser = userRepository.save(user);

        // 평문 시크릿 메모리에서 제거
        savedUser.clearPlainSecret();

        // DTO 변환 후 반환
        return UserIdResponseDto.from(savedUser);

    }
}
