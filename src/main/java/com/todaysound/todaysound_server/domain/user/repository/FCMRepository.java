package com.todaysound.todaysound_server.domain.user.repository;

import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FCMRepository extends JpaRepository<FCM_Token, Long> {
    List<FCM_Token> findByUser(User user);

    FCM_Token findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM FCM_Token ft WHERE ft.fcmToken IN :fcmTokens")
    void deleteAllByFcmTokenIn(@Param("fcmTokens") List<String> fcmTokens);
}
