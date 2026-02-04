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
import lombok.extern.slf4j.Slf4j;

/**
 * Audit Log AOP — 민감 작업 감사 로그 기록.
 *
 * @AuditLog 적용 메서드의 액션·URI·사용자·결과를 로깅한다.
 *           인증 실패·권한 부족 시도 모니터링 (RULE 1.4.2).
 *           SLF4J 파라미터화 로깅 사용 (RULE 1.4.3).
 *
 * @see AuditLog
 */
@Slf4j
@Aspect
@Component
@Order(90)
public class AuditLogAspect {

    @Pointcut("@annotation(auditLog)")
    public void auditLogPointcut(AuditLog auditLog) {
    }

    @Around("auditLogPointcut(auditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String action = auditLog.value().isEmpty() ? joinPoint.getSignature().toShortString() : auditLog.value();
        String uri = getRequestUri();
        String user = getCurrentUser();

        try {
            Object result = joinPoint.proceed();
            log.info("AUDIT action={} uri={} user={} status=SUCCESS", action, uri, user);
            return result;
        } catch (Throwable e) {
            log.warn("AUDIT action={} uri={} user={} status=FAILED reason={}", action, uri, user, e.getMessage());
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

    private String getCurrentUser() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getRemoteUser)
                .orElse("anonymous");
    }
}
