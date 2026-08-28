package com.todaysound.todaysound_server.global.application;

import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenLifecycleService {

    private final FCMRepository fcmRepository;

    /**
     * 트랜잭션 없이 실행되는 직접 발송 경로에서도 토큰 무효화만 독립적으로 커밋한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivateAllIfTokenMatches(Collection<FcmTarget> attemptedTokens) {
        if (attemptedTokens.isEmpty()) {
            return;
        }

        LocalDateTime invalidatedAt = LocalDateTime.now();
        attemptedTokens.forEach(attemptedToken -> fcmRepository.deactivateIfTokenMatches(
                attemptedToken.referenceId(),
                attemptedToken.token(),
                invalidatedAt
        ));
    }
}
