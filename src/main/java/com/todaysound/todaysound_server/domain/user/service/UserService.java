package com.todaysound.todaysound_server.domain.user.service;

import com.todaysound.todaysound_server.domain.user.dto.request.UserSecretRequest;
import com.todaysound.todaysound_server.domain.user.dto.response.UserIdResponse;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.factory.UserFactory;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final UserQueryService userQueryService;
    private final HeaderAuthValidator headerAuthValidator;

    public UserIdResponse anonymous(UserSecretRequest userSecretRequest) {

        log.info("Anonymous user command received");
        boolean secretExists = userQueryService.existsBySecretFingerprint(userSecretRequest.deviceSecret());

        User user;

        if (!secretExists) {
            log.info("User secret does not exist, creating new user");
            log.info("fcmToken: {}", userSecretRequest.fcmToken());

            User newUser = userFactory.createAnonymousUser(userSecretRequest);

            FCM_Token fcmToken = FCM_Token.create(newUser, userSecretRequest.fcmToken(), userSecretRequest.model());

            newUser.addFcmToken(fcmToken);

            user = userRepository.save(newUser);

            user.clearPlainSecret();
        } else {
            log.info("User secret exists, returning existing user");
            user = userQueryService.findBySecretFingerprint(userSecretRequest.deviceSecret());
        }

        return UserIdResponse.from(user);

    }

    public void withdraw(String userUuid, String deviceSecret) {
        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        userRepository.delete(user);
    }
}
