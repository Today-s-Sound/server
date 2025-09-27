package com.todaysound.todaysound_server.domain.user.validator;

import com.todaysound.todaysound_server.domain.user.exception.AuthErrorCode;
import com.todaysound.todaysound_server.domain.user.service.UserQueryService;
import com.todaysound.todaysound_server.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserQueryService userQueryService;

    public void validateUniqueSecret(String deviceSecret) {
        if(userQueryService.existsBySecretFingerprint(deviceSecret)){
            throw BaseException.type(AuthErrorCode.DEVICE_SECRET_ALREADY_EXISTED);
        }
    }

}
