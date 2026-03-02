package com.todaysound.todaysound_server.domain.alarm.entity;

public enum OutboxStatus {
    PENDING,  // 전송 대기
    SENT,     // 전송 완료
    FAILED    // 최대 재시도 횟수 초과
}
