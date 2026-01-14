package com.todaysound.todaysound_server.support;


import com.todaysound.todaysound_server.support.isolation.DatabaseIsolation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@DatabaseIsolation
@ActiveProfiles("ci")
public abstract class ServiceTestSupport {


}
