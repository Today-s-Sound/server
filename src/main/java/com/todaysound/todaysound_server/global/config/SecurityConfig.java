package com.todaysound.todaysound_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http

                 //CSRF 비활성화
                 .csrf(AbstractHttpConfigurer::disable)

                 // session 사용 X (JWT 사용)
                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 
                 // form login 비활성화
                 .formLogin(AbstractHttpConfigurer::disable)

                //http Basic 인증 비활성화 - jwt 구현 전 임시 활성화
                //.httpBasic(AbstractHttpConfigurer::disable)

                //URL별 권한 설정
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                            ""
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); //임시로 httpBasic 활성화

        return http.build();
    }
}
