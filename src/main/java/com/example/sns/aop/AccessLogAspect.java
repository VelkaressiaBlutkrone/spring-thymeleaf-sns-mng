package com.example.sns.aop;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Access Log AOP.
 * RULE 3.5.1: 횡단 관심사(로깅) 전용.
 * RULE 3.5.2: 제거해도 시스템 정상 동작 (보조 역할).
 * RULE 3.5.3: Annotation 기반 Pointcut.
 * RULE 3.5.6: 상태 변경 금지, 관찰자 역할만.
 * RULE 3.5.7: @Order 명시.
 */
@Slf4j
@Aspect
@Component
@Order(100)
@RequiredArgsConstructor
public class AccessLogAspect {

    @Pointcut("@annotation(com.example.sns.aop.LogAccess)")
    public void logAccessPointcut() {
    }

    @Around("logAccessPointcut()")
    public Object logAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();
        String uri = getRequestUri();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("ACCESS [{}] {} {}ms", method, uri, elapsed);
            return result;
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("ACCESS [{}] {} {}ms FAILED: {}", method, uri, elapsed, e.getMessage());
            throw e;
        }
    }

    private String getRequestUri() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getRequestURI)
                .orElse("unknown");
    }
}
