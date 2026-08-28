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

    /**
     * 발송 당시 토큰과 현재 토큰이 같을 때만 비활성화해,
     * 늦은 UNREGISTERED 응답이 이미 갱신된 토큰을 끄지 않게 한다.
     */
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
