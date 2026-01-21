package com.todaysound.todaysound_server.domain.summary.repository;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {

    /**
     * Summary ID로 조회
     */
    Optional<Summary> findById(Long id);

    /**
     * 생성일 기준으로 오래된 Summary 삭제
     */
    void deleteByCreatedAtBefore(LocalDateTime dateTime);

}

