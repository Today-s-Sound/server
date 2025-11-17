package com.todaysound.todaysound_server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FCMConfig {

    // application.yml을 통해 환경변수(FCM_SECRET_STRING) 값을 주입받음
    @Value("${fcm.secret-string}")
    private String fcmSecretString;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount;

            // 환경 변수(GitHub Secrets)가 존재하면 우선 사용 (Prod 환경)
            if (fcmSecretString != null && !fcmSecretString.isBlank()) {
                log.info("🔑 Firebase 키를 [환경 변수]에서 로드합니다.");
                serviceAccount = new ByteArrayInputStream(fcmSecretString.getBytes(StandardCharsets.UTF_8));
            }
            else {
                String path = "todaysound-68df8-firebase-adminsdk-fbsvc-6b2b6e6a71.json";
                ClassPathResource resource = new ClassPathResource(path);

                if (!resource.exists()) {
                    log.error("❌ Firebase 키 파일을 찾을 수 없습니다: {}", path);
                    throw new IOException("Firebase key file not found");
                }

                log.info("🔑 Firebase 키를 [로컬 파일]에서 로드합니다: {}", path);
                serviceAccount = resource.getInputStream();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK가 성공적으로 초기화되었습니다.");
            } else {
                log.info("ℹ️ Firebase Admin SDK가 이미 초기화되어 있습니다.");
            }

        } catch (Exception e) {
            log.error("❌ Firebase Admin SDK 초기화 실패", e);
        }
    }
}
