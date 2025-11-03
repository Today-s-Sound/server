package com.todaysound.todaysound_server.domain.subscription.validator;

import com.todaysound.todaysound_server.domain.subscription.exception.SubscriptionException;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.global.exception.BaseException;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionValidator {

    public void validateCreatable(User user, String url) {
        if (url == null || url.isBlank()) {
            throw BaseException.type(SubscriptionException.SUBSCRIPTION_INVALID_URL);
        }
        // 중복 URL 허용: 같은 URL을 여러 번 구독할 수 있음
    }
}


