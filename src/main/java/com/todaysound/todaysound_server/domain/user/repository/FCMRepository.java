package com.todaysound.todaysound_server.domain.user.repository;

import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FCMRepository extends JpaRepository<FCM_Token, Long> {
    List<FCM_Token> findByUser(User user);
    void deleteAllByFcmTokenIn(List<String> fcmTokens);
}
