package com.example.sns.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 예외 처리 보조 AOP — Controller 예외 로깅.
 *
 * Controller 예외 발생 시 상세 로깅 후 재throw.
 * 예외 판단·변환 금지, GlobalExceptionHandler가 처리 (RULE 3.5.6).
 * SLF4J 파라미터화 로깅, ERROR 레벨 사용 (RULE 1.4.3).
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
