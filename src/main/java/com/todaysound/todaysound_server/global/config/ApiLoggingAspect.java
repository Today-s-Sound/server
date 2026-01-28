package com.todaysound.todaysound_server.global.config;

import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApiLoggingAspect {

    @Pointcut("execution(* com.todaysound.todaysound_server..controller..*(..)) || "
            + "execution(* com.todaysound.todaysound_server..presentation..*(..))")
    private void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("API 요청 시작 {} {}",
                kv("class", className),
                kv("method", methodName));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;

            log.info("API 요청 완료 {} {} {}",
                    kv("class", className),
                    kv("method", methodName),
                    kv("elapsedMs", elapsed));

            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - startTime;

            log.warn("API 요청 실패 {} {} {} {}",
                    kv("class", className),
                    kv("method", methodName),
                    kv("elapsedMs", elapsed),
                    kv("exception", ex.getClass().getSimpleName()));

            throw ex;
        }
    }
}
