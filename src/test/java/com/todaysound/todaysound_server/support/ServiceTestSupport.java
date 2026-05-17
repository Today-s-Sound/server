package com.todaysound.todaysound_server.support;


import com.todaysound.todaysound_server.global.application.FirebaseMessagingClient;
import com.todaysound.todaysound_server.support.isolation.DatabaseIsolation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DatabaseIsolation
@ActiveProfiles("ci")
public abstract class ServiceTestSupport {

    @MockitoBean
    protected FirebaseMessagingClient firebaseMessagingClient;

}
