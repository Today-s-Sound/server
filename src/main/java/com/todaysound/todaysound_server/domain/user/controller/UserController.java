package com.todaysound.todaysound_server.domain.user.controller;

import com.todaysound.todaysound_server.domain.user.service.UserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.todaysound.todaysound_server.domain.user.dto.request.UserSecretRequestDto;
import com.todaysound.todaysound_server.domain.user.dto.response.UserIdResponseDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserCommandService userCommandService;

    @Override
    @PostMapping("/anonymous")
    public UserIdResponseDto anonymous(@Valid @RequestBody UserSecretRequestDto userSecretRequestDto) {
        return userCommandService.anonymous(userSecretRequestDto);
    }

}
