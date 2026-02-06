package com.example.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example..service..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        String args = Arrays.toString(joinPoint.getArgs());
        log.info("[Before] {} args={}", method, args);
    }

    @AfterReturning(pointcut = "execution(* com.example..service..*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String method = joinPoint.getSignature().toShortString();
        log.info("[AfterReturning] {} result={}", method, result);
    }

    @AfterThrowing(pointcut = "execution(* com.example..service..*(..))", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String method = joinPoint.getSignature().toShortString();
        log.warn("[AfterThrowing] {} error={}", method, ex.getMessage());
    }
}
