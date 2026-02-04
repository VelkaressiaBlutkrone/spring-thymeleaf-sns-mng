package com.example.sns.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 예외 처리 보조 AOP.
 * RULE 3.5.4: 예외 catch 시 로깅만 하고 반드시 재throw.
 * RULE 3.5.4: 예외 판단·변환·return null 금지.
 * RULE 3.5.7: @Order 명시.
 * RULE 1.4.2: 인증 실패·권한 부족 시도 로깅.
 */
@Slf4j
@Aspect
@Component
@Order(80)
public class ExceptionLoggingAspect {

    @Pointcut("execution(* com.example.sns.controller..*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object logException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error("Controller exception: {} - {}", joinPoint.getSignature().toShortString(), e.getMessage(), e);
            throw e;
        }
    }
}
