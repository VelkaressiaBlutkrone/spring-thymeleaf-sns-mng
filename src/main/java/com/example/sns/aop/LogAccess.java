package com.example.sns.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Access Log AOP 적용 대상 메서드 표시.
 *
 * Annotation 기반 Pointcut 사용 (RULE 3.5.4).
 *
 * @see AccessLogAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAccess {
}
