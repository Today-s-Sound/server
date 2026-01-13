package com.todaysound.todaysound_server.domain.user.controller;

import com.todaysound.todaysound_server.domain.user.dto.request.UserSecretRequest;
import com.todaysound.todaysound_server.domain.user.dto.response.UserIdResponse;
import com.todaysound.todaysound_server.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @PostMapping("/anonymous")
    public UserIdResponse anonymous(
            @Valid @RequestBody UserSecretRequest userSecretRequest) {
        return userService.anonymous(userSecretRequest);
    }

    @Override
    @DeleteMapping("/withdraw")
    @ResponseStatus(HttpStatus.OK)
    public void withdraw(@RequestHeader("X-User-ID") String userUuid,
                         @RequestHeader("X-Device-Secret") String deviceSecret) {
        userService.withdraw(userUuid, deviceSecret);
    }

}
