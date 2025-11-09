package com.todaysound.todaysound_server.domain.user.service;

import com.todaysound.todaysound_server.domain.user.dto.request.UserSecretRequestDto;
import com.todaysound.todaysound_server.domain.user.dto.response.UserIdResponseDto;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.exception.AuthErrorCode;
import com.todaysound.todaysound_server.domain.user.factory.UserFactory;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.domain.user.validator.UserValidator;
import com.todaysound.todaysound_server.global.exception.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserFactory userFactory;
    private final UserQueryService userQueryService;

    public UserIdResponseDto anonymous(UserSecretRequestDto userSecretRequestDto) {

        log.info("Anonymous user command received");
        boolean secretExists =
                userQueryService.existsBySecretFingerprint(userSecretRequestDto.deviceSecret());

        User savedUser;

        if (secretExists) {
            log.info("User secret already exists");
            User existingUser =
                    userQueryService.findBySecretFingerprint(userSecretRequestDto.deviceSecret());

            if (existingUser.hasFcmToken(userSecretRequestDto.fcmToken())) {
                throw BaseException.type(AuthErrorCode.FCM_TOKEN_ALREADY_EXISTED);
            }

            // 중복 시크릿이 있고 fcmToken이 없다면 fcmToken만 추가
            FCM_Token newFcmToken = FCM_Token.builder().fcmToken(userSecretRequestDto.fcmToken())
                    .model(userSecretRequestDto.model()).user(existingUser).build();

            log.info("New FCM_Token: {}", newFcmToken.getFcmToken());
            existingUser.addFcmToken(newFcmToken);
            savedUser = userRepository.save(existingUser);

        } else {
            log.info("User secret does not exist");
            // 중복 시크릿이 없다면 새로 유저 만들기
            User newUser = userFactory.createAnonymousUser(userSecretRequestDto);

            // FCM Token 추가
            FCM_Token fcmToken = FCM_Token.builder().fcmToken(userSecretRequestDto.fcmToken())
                    .model(userSecretRequestDto.model()).user(newUser).build();

            newUser.addFcmToken(fcmToken);

            // 데이터베이스에 저장
            savedUser = userRepository.save(newUser);
        }

        // 평문 시크릿 메모리에서 제거
        savedUser.clearPlainSecret();

        // DTO 변환 후 반환
        return UserIdResponseDto.from(savedUser);

    }
}
