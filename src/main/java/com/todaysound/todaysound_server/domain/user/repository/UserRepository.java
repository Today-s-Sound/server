package com.todaysound.todaysound_server.domain.user.repository;

import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.entity.UserType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자 ID로 사용자 조회
     */
    Optional<User> findByUserId(String userId);

    /**
     * 고정 출력 지문(fingerprint)으로 사용자 존재 여부 확인
     */
    boolean existsBySecretFingerprint(String secretFingerprint);

    /**
     * 고정 출력 지문(fingerprint)으로 사용자 조회
     */
    Optional<User> findBySecretFingerprint(String secretFingerprint);

    /**
     * 활성 사용자만 조회
     */
    List<User> findByIsActiveTrue();

    /**
     * 사용자 타입별 조회
     */
    List<User> findByUserType(UserType userType);
}
