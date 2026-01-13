package com.todaysound.todaysound_server.global.utils;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class LogMarkers {

    /**
     * 핵심 비즈니스 로직 (구독 생성, 삭제 등)
     */
    public static final Marker BUSINESS = MarkerFactory.getMarker("BUSINESS");

    // ============================================
    // 핵심 비즈니스 로그 (반드시 모니터링 필요)
    // ============================================
    /**
     * 사용자 인증/인가 관련
     */
    public static final Marker AUTH = MarkerFactory.getMarker("AUTH");
    /**
     * 결제/과금 관련 (최우선 모니터링)
     */
    public static final Marker PAYMENT = MarkerFactory.getMarker("PAYMENT");
    /**
     * 외부 API 호출 (FCM, 외부 서비스 등)
     */
    public static final Marker EXTERNAL_API = MarkerFactory.getMarker("EXTERNAL_API");
    /**
     * 즉시 대응 필요한 심각한 문제
     */
    public static final Marker CRITICAL = MarkerFactory.getMarker("CRITICAL");

    // ============================================
    // 심각도 높은 로그
    // ============================================
    /**
     * 알람이 필요한 중요 이벤트
     */
    public static final Marker ALERT = MarkerFactory.getMarker("ALERT");
    /**
     * API 성능 측정
     */
    public static final Marker PERFORMANCE = MarkerFactory.getMarker("PERFORMANCE");

    // ============================================
    // 성능/모니터링 로그
    // ============================================
    /**
     * 스케줄러/배치 작업
     */
    public static final Marker SCHEDULER = MarkerFactory.getMarker("SCHEDULER");

    private LogMarkers() {
        // 유틸리티 클래스
    }
}

