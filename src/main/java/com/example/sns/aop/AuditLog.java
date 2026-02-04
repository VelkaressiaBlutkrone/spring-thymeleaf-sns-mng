package com.example.sns.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audit Log AOP 적용 대상 표시.
 * 민감 작업(비밀번호 변경, 역할 변경 등) 감사 로그용.
 * RULE 3.5.3: Annotation 기반 Pointcut 우선 사용.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String value() default "";
}
