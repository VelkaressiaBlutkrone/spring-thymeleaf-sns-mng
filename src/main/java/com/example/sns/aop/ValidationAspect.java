package com.example.sns.aop;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Valid 통합 검사 AOP — 메서드 파라미터 검증.
 *
 * @ValidCheck 적용 메서드의 @Valid 파라미터를 일괄 검증한다.
 * @RequestBody 외 @PathVariable·@RequestParam 등 @Valid 대상도 통합 검증 (RULE 1.3).
 * 검증 실패 시 ConstraintViolationException → GlobalExceptionHandler가 ErrorResponse 변환.
 * 예외 기록 후 재throw (RULE 3.5.6).
 *
 * @see ValidCheck
 * @see com.example.sns.exception.GlobalExceptionHandler#handleConstraintViolation
 */
@Slf4j
@Aspect
@Component
@Order(70)
@RequiredArgsConstructor
public class ValidationAspect {

    private final Validator validator;

    @Pointcut("@annotation(com.example.sns.aop.ValidCheck)")
    public void validCheckPointcut() {
    }

    /**
     * @Valid 파라미터 검증 후 원본 실행.
     * 검증 실패 시 ConstraintViolationException 발생 (GlobalExceptionHandler 처리).
     *
     * @param joinPoint 대상 메서드
     * @return 원본 메서드 반환값
     * @throws Throwable 원본 메서드 예외 또는 검증 실패 시 ConstraintViolationException
     */
    @Around("validCheckPointcut()")
    public Object validateAndProceed(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        java.lang.reflect.Parameter[] parameters = signature.getMethod().getParameters();

        Set<ConstraintViolation<?>> allViolations = new HashSet<>();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Valid.class) && args[i] != null) {
                Set<ConstraintViolation<Object>> violations = validator.validate(args[i]);
                allViolations.addAll(violations);
            }
        }

        if (!allViolations.isEmpty()) {
            List<String> messages = allViolations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .toList();
            log.warn("Validation failed [{}]: {}", signature.toShortString(), messages);
            throw new ConstraintViolationException("Validation failed", allViolations);
        }

        return joinPoint.proceed();
    }
}
