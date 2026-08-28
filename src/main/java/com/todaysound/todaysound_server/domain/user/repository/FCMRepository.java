package com.todaysound.todaysound_server.domain.user.repository;

import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FCMRepository extends JpaRepository<FCM_Token, Long> {
    List<FCM_Token> findByUser(User user);

    List<FCM_Token> findByUserAndIsActiveTrue(User user);

    FCM_Token findByUserId(Long userId);

    @Modifying
    @Query("""
            UPDATE FCM_Token token
            SET token.isActive = false,
                token.invalidatedAt = :invalidatedAt
            WHERE token.id = :tokenId
              AND token.fcmToken = :attemptedToken
              AND token.isActive = true
            """)
    int deactivateIfTokenMatches(
            @Param("tokenId") Long tokenId,
            @Param("attemptedToken") String attemptedToken,
            @Param("invalidatedAt") LocalDateTime invalidatedAt
    );
}
