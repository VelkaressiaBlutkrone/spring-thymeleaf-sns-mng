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
 * Access Log AOP — API 접근 로그 기록.
 *
 * @LogAccess 적용 메서드의 URI, 소요 시간을 로깅한다.
 *            횡단 관심사 전용, 제거해도 시스템 정상 동작 (RULE 3.5.1, 3.5.2).
 *            SLF4J 파라미터화 로깅 사용 (RULE 1.4.3).
 *
 * @see LogAccess
 */
@Slf4j
@Aspect
@Component
@Order(100)
@RequiredArgsConstructor
public class AccessLogAspect {

    /** @LogAccess 어노테이션이 붙은 메서드 대상 */
    @Pointcut("@annotation(com.example.sns.aop.LogAccess)")
    public void logAccessPointcut() {
    }

    /**
     * 접근 로그 기록 후 원본 실행. 예외 시 로깅 후 재throw (RULE 3.5.4).
     *
     * @param joinPoint 대상 메서드
     * @return 원본 메서드 반환값
     * @throws Throwable 원본 메서드 예외 (로깅 후 그대로 전파)
     */
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
