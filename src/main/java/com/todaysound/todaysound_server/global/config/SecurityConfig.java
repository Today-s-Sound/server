package com.todaysound.todaysound_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
                // CSRF 비활성화 (개발 단계)
                .csrf(AbstractHttpConfigurer::disable)

                // 모든 요청에 대해 인증 없이 접근 허용 (개발 단계)
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/**").permitAll() // API
                                                                                           // 엔드포인트
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger
                                                                                          // UI
                        .anyRequest().permitAll())

                // 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                //URL별 권한 설정
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/users/anonymous").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); //임시로 httpBasic 활성화

        return http.build();
    }
}

