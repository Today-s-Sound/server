package com.todaysound.todaysound_server.domain.user.service;

import com.todaysound.todaysound_server.domain.user.dto.request.UserSecretRequest;
import com.todaysound.todaysound_server.domain.user.dto.response.UserIdResponse;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.factory.UserFactory;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.domain.user.validator.HeaderAuthValidator;
import static com.todaysound.todaysound_server.global.utils.LogMarkers.AUTH;
import static net.logstash.logback.argument.StructuredArguments.kv;

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

        log.info(AUTH, "익명 사용자 인증 요청 수신");
        boolean secretExists = userQueryService.existsBySecretFingerprint(userSecretRequest.deviceSecret());

        User user;

        if (!secretExists) {
            log.info(AUTH, "신규 사용자 생성 시작");

            User newUser = userFactory.createAnonymousUser(userSecretRequest);

            FCM_Token fcmToken = FCM_Token.create(newUser, userSecretRequest.fcmToken(), userSecretRequest.model());

            newUser.addFcmToken(fcmToken);

            user = userRepository.save(newUser);

            log.info(AUTH, "신규 사용자 생성 완료 {}", kv("userId", user.getId()));

            user.clearPlainSecret();
        } else {
            log.info(AUTH, "기존 사용자 인증 완료");
            user = userQueryService.findBySecretFingerprint(userSecretRequest.deviceSecret());
        }

        return UserIdResponse.from(user);

    }

    public void withdraw(String userUuid, String deviceSecret) {
        // 헤더 인증 검증 및 사용자 획득
        User user = headerAuthValidator.validateAndGetUser(userUuid, deviceSecret);

        userRepository.delete(user);

        log.info(AUTH, "회원 탈퇴 완료 {}", kv("userId", user.getId()));
    }
}
