package com.todaysound.todaysound_server.domain.user.dto.response;

import com.todaysound.todaysound_server.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserIdResponse(
        @Schema(description = "외부 노출용 사용자 ID", example = "user-uuid-1234")
        String userId
) {
    public static UserIdResponse from(User user) {
        return new UserIdResponse(user.getUserId());
    }

}
