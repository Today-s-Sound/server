package com.todaysound.todaysound_server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        // API 정보 설정
        Info info = new Info().title("Today Sound API") // 제목
                .description("오늘의 소리 API 명세서") // 설명
                .version("1.0.0"); // 버전

        String jwtSchemeName = "JWT";
        SecurityRequirement securityRequirement =
                new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components().addSecuritySchemes(jwtSchemeName,
                new SecurityScheme().name(jwtSchemeName) // 스키마 이름
                        .type(SecurityScheme.Type.HTTP) // HTTP 타입
                        .scheme("bearer") // Bearer 토큰
                        .bearerFormat("JWT")); // JWT 형식

        return new OpenAPI().addServersItem(new Server().url("/")) // 서버 URL
                .info(info) // API 정보
                .addSecurityItem(securityRequirement) // 보안 요구사항
                .components(components); // 컴포넌트
    }
}
