package com.todaysound.todaysound_server.global.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtil {

    public static String toRelativeTime(LocalDateTime past) {
        if (past == null) {
            return "";
        }

        Duration duration = Duration.between(past, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "방금 전";
        }
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + "분 전";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "시간 전";
        }
        long days = duration.toDays();
        return days + "일 전";
    }
}
