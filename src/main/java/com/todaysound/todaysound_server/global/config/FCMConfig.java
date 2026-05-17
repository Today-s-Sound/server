package com.todaysound.todaysound_server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import static com.todaysound.todaysound_server.global.utils.LogMarkers.CRITICAL;
import static com.todaysound.todaysound_server.global.utils.LogMarkers.EXTERNAL_API;
import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

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

            log.info("FCM Secret String 상태 확인 {} {} {}",
                    kv("isNull", fcmSecretString == null),
                    kv("isBlank", fcmSecretString != null && fcmSecretString.isBlank()),
                    kv("length", fcmSecretString != null ? fcmSecretString.length() : 0));

            // 환경 변수(GitHub Secrets)가 존재하면 우선 사용 (Prod 환경)
            if (fcmSecretString != null && !fcmSecretString.isBlank()) {
                log.info("Firebase 키를 환경 변수에서 로드합니다 {}", kv("source", "env"));

                byte[] decoded = Base64.getDecoder().decode(fcmSecretString);
                String jsonString = new String(decoded, StandardCharsets.UTF_8);

                if (!jsonString.trim().startsWith("{")) {
                    log.error(CRITICAL, "디코딩된 데이터가 올바른 JSON 형식이 아닙니다");
                }


                serviceAccount =
                        new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

                // FirebaseOptions 생성 및 초기화
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount)
                                .createScoped(Arrays.asList(
                                        "https://www.googleapis.com/auth/firebase.messaging")))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info(EXTERNAL_API, "Firebase Admin SDK 초기화 완료 {}", kv("source", "env"));
                } else {
                    log.info(EXTERNAL_API, "Firebase Admin SDK 이미 초기화됨 {}", kv("source", "env"));
                }
            } else {
                log.info("Firebase 키를 로컬 파일에서 로드합니다 {}", kv("source", "local"));
                ClassPathResource resource = new ClassPathResource(
                        "todaysound-68df8-firebase-adminsdk-fbsvc-6b2b6e6a71.json");
                serviceAccount = resource.getInputStream();

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info(EXTERNAL_API, "Firebase Admin SDK 초기화 완료 {}", kv("source", "local"));
                } else {
                    log.info(EXTERNAL_API, "Firebase Admin SDK 이미 초기화됨 {}", kv("source", "local"));
                }
            }

        } catch (Exception e) {
            log.error(CRITICAL, "Firebase Admin SDK 초기화 실패 {}", kv("exception", e.getMessage()), e);
        }

    }
}
