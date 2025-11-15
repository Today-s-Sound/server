package com.todaysound.todaysound_server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Slf4j
@Configuration
public class FCMConfig {

    @PostConstruct
    public void initialize() {
        try {
            String path = "todaysound-68df8-firebase-adminsdk-fbsvc-6b2b6e6a71.json";
            ClassPathResource resource = new ClassPathResource(path);

            log.info("🔑 Firebase 키 파일 경로: {}", resource.exists());
            InputStream serviceAccount = resource.getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK가 성공적으로 초기화되었습니다.");
            }
        } catch (Exception e) {
            log.error("❌ Firebase Admin SDK 초기화 실패", e);
        }
    }
}