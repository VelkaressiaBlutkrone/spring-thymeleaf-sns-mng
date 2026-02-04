package com.example.sns.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Access Log AOP 적용 대상 표시.
 * RULE 3.5.3: Annotation 기반 Pointcut 우선 사용.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAccess {
}
