package com.todaysound.todaysound_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // session 사용 X (JWT 사용)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // form login 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // URL별 권한 설정
                .authorizeHttpRequests(requests -> requests
                        // 앱 공개 API
                        .requestMatchers("/api/users/anonymous").permitAll()
                        .requestMatchers("/api/alarms/**").permitAll()
                        .requestMatchers("/api/subscriptions/**").permitAll() // 구독 API 허용
                        .requestMatchers("/api/fcm/**").permitAll() // FCM API 허용 (테스트용)
                        .requestMatchers("/api/feeds/**").permitAll() // Feed API 허용

                        // 크롤러 전용 내부 API (로컬/내부 네트워크에서만 접근한다고 가정)
                        .requestMatchers("/internal/**").permitAll()

                        // 문서 및 헬스체크
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/", "/actuator/health").permitAll()

                        // 관리자 페이지
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 기타는 인증 필요
                        .anyRequest().authenticated())

                .httpBasic(Customizer.withDefaults()); // 임시로 httpBasic 활성화

        return http.build();
    }
}

