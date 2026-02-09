# AOP 문서

> RULE 3.5.12: AOP 추가 시 문서화 필수.

## 개요

본 프로젝트의 AOP는 **횡단 관심사(Cross-Cutting Concern)** 전용으로, RULE 3.5를 준수한다.

## 적용 대상 (Pointcut)

| Aspect | Pointcut | 적용 범위 |
|--------|----------|-----------|
| ValidationAspect | `@annotation(ValidCheck)` | `@ValidCheck` 어노테이션이 붙은 메서드 |
| AccessLogAspect | `@annotation(LogAccess)` | `@LogAccess` 어노테이션이 붙은 메서드 |
| AuditLogAspect | `@annotation(AuditLog)` | `@AuditLog` 어노테이션이 붙은 메서드 |
| ExceptionLoggingAspect | `execution(* com.example.sns.controller..*(..))` | `controller` 패키지 하위 모든 메서드 |

## 목적

| Aspect | 목적 |
|--------|------|
| ValidationAspect | @Valid 파라미터 통합 검증 (@RequestBody 외 @PathVariable·@RequestParam 등) |
| AccessLogAspect | API 접근 로그 (URI, 메서드, 소요 시간) 기록 |
| AuditLogAspect | 민감 작업 감사 로그 (비밀번호 변경, 역할 변경 등) 기록 |
| ExceptionLoggingAspect | Controller 예외 발생 시 로깅 (GlobalExceptionHandler 전 보조) |

## 실행 시점

| Aspect | Advice | 실행 시점 |
|--------|--------|-----------|
| ValidationAspect | @Around | 메서드 실행 전 @Valid 파라미터 검증 |
| AccessLogAspect | @Around | 메서드 실행 전후 |
| AuditLogAspect | @Around | 메서드 실행 전후 |
| ExceptionLoggingAspect | @Around | 메서드 실행 중 예외 발생 시 catch 후 재throw |

## 예외 처리 정책

- **RULE 3.5.6 준수**: 모든 Aspect는 예외를 **기록만** 하고 **반드시 재throw** 한다.
- 예외 판단·변환·return null 금지.
- 예외 처리(ErrorResponse 변환)는 GlobalExceptionHandler가 담당.

## @Order

| 순서 | Aspect | Order 값 |
|------|--------|----------|
| 1 | ValidationAspect | 70 |
| 2 | ExceptionLoggingAspect | 80 |
| 3 | AuditLogAspect | 90 |
| 4 | AccessLogAspect | 100 |

낮은 Order 값이 먼저 실행된다. (Validation → Exception → Audit → Access)

## 제거 시 영향도

| Aspect | 제거 시 영향 |
|--------|--------------|
| ValidationAspect | @ValidCheck 미적용 시 @RequestBody는 Spring 기본 검증만. **시스템 동작에는 영향 없음.** |
| AccessLogAspect | Access 로그 미기록. **시스템 동작에는 영향 없음.** |
| AuditLogAspect | 감사 로그 미기록. **시스템 동작에는 영향 없음.** |
| ExceptionLoggingAspect | Controller 예외 시 상세 로그 미기록. GlobalExceptionHandler는 정상 동작. **시스템 동작에는 영향 없음.** |

**RULE 3.5.2**: AOP 제거해도 시스템이 정상 동작해야 한다.

## 로깅 규칙 (RULE 1.4.3)

- **SLF4J** `@Slf4j` 또는 `LoggerFactory.getLogger(Class)` 사용
- **파라미터화 로깅** `{}` placeholder 사용, 문자열 concat 금지
- **로그 레벨**: ERROR(예외·실패), WARN(비정상·재시도), INFO(주요 이벤트), DEBUG(개발용)
- **금지 (1.4.3.7)**: PII·토큰·비밀번호 로그 출력, `System.out`/`System.err`, 예외 catch 후 로그 없이 진행
- **모니터링 (1.4.3.8)**: ERROR 이상 즉시 알림, WARN 누적 주간 리포트

## 사용 방법

### Access Log 적용

```java
@LogAccess
@PostMapping("/api/example")
public ResponseEntity<?> example() { ... }
```

### Audit Log 적용

```java
@AuditLog("PASSWORD_CHANGE")
@PostMapping("/api/users/password")
public ResponseEntity<?> changePassword() { ... }
```

### @Valid 통합 검사 적용

```java
@ValidCheck
@PostMapping("/api/example")
public ResponseEntity<?> example(@Valid @RequestBody SomeRequest request) { ... }
```

`@ValidCheck`가 붙은 메서드의 `@Valid` 파라미터가 AOP에서 일괄 검증된다.
검증 실패 시 `ConstraintViolationException` → GlobalExceptionHandler가 ErrorResponse로 변환.

---

**마지막 업데이트**: 2026-02-09
