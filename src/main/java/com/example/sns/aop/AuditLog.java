package com.example.sns.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audit Log AOP 적용 대상 메서드 표시.
 *
 * 민감 작업(비밀번호 변경, 역할 변경 등) 감사 로그용.
 * Annotation 기반 Pointcut 사용 (RULE 3.5.3).
 *
 * @param value 감사 액션명 (미지정 시 메서드 시그니처 사용)
 * @see AuditLogAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String value() default "";
}
