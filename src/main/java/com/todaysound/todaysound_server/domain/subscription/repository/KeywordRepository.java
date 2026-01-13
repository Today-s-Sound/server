package com.todaysound.todaysound_server.domain.subscription.repository;

import com.todaysound.todaysound_server.domain.subscription.entity.Keyword;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    /**
     * 키워드 이름으로 키워드 조회
     */
    Optional<Keyword> findByName(String name);

}

