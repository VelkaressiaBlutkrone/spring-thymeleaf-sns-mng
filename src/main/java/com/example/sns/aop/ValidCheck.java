package com.example.sns.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Valid 통합 검사 AOP 적용 대상 메서드 표시.
 *
 * 메서드 파라미터 중 @Valid가 붙은 객체를 AOP에서 일괄 검증한다.
 * Spring의 @RequestBody 자동 검증과 병행하여, @PathVariable·@RequestParam 등
 * 기타 @Valid 파라미터도 통합 검증 (RULE 1.3).
 * Annotation 기반 Pointcut 사용 (RULE 3.5.3).
 *
 * @see ValidationAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCheck {
}
