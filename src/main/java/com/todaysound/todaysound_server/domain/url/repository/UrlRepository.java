package com.todaysound.todaysound_server.domain.url.repository;

import com.todaysound.todaysound_server.domain.url.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<Url, Long> {


}
