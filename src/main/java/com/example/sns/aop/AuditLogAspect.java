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
 * Audit Log AOP.
 * RULE 1.4.2: 민감 작업 감사 로그.
 * RULE 3.5.1: 횡단 관심사(감사 로깅) 전용.
 * RULE 3.5.3: Annotation 기반 Pointcut.
 * RULE 3.5.6: 상태 변경 금지.
 * RULE 3.5.7: @Order 명시.
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
