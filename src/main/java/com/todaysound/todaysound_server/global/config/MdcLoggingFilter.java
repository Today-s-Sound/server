package com.todaysound.todaysound_server.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final String REQUEST_URI = "requestUri";
    private static final String HTTP_METHOD = "httpMethod";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 고유한 trace ID 생성
            String traceId = generateTraceId();
            MDC.put(TRACE_ID, traceId);

            // 요청 정보 저장
            MDC.put(REQUEST_URI, request.getRequestURI());
            MDC.put(HTTP_METHOD, request.getMethod());

            // 헤더에서 사용자 정보 추출
            String userUuid = request.getHeader("X-User-Uuid");
            if (userUuid != null && !userUuid.isEmpty()) {
                MDC.put(USER_ID, userUuid);
            }

            // 응답 헤더에 traceId 추가
            response.setHeader("X-Trace-Id", traceId);

            filterChain.doFilter(request, response);
        } finally {
            // 메모리 누수 방지
            MDC.clear();
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

