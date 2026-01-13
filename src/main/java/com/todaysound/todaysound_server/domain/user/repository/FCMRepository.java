package com.todaysound.todaysound_server.domain.user.repository;

import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FCMRepository extends JpaRepository<FCM_Token, Long> {
    List<FCM_Token> findByUser(User user);

    FCM_Token findByUserId(Long userId);

    void deleteAllByFcmTokenIn(List<String> fcmTokens);
}
