# 프로젝트 개발 규칙 (RULE Book)

> **이 규칙은 프로젝트의 도메인, 규모, 일정과 무관하게 반드시 준수한다.**
> **예외가 필요한 경우 반드시 기술 리더의 승인과 문서화를 거친다.**
> **규칙 위반은 코드 리뷰에서 반려 사유가 된다.**

### 운영 원칙 (v1.0.5)

> **보안과 가용성이 충돌할 경우**, 최우선적으로 **보안을 유지하면서 가용성을 확보할 수 있는 대안**(예: Grace Period)을 설계한다.  
> **가용성을 위해 보안을 포기하는 결정**은 기술 리더의 서면 승인이 없는 한 절대 금지한다.

---

## RULE 레벨

| 레벨 | 의미 |
|------|------|
| **MUST** | 위반 시 즉시 반려 |
| **SHOULD** | 원칙, 예외 시 문서화 |
| **RECOMMENDED** | 권장 |

---

## 1. 보안(Security) RULE — 절대 예외 없음 [MUST]

### 1.1 비밀정보 관리

#### 1.1.1 반드시 지켜야 함

- 비밀번호, 토큰, API Key, 인증서 **소스코드 하드코딩 금지**
- `.env`, Secret Manager, Vault, KMS 등 **외부 주입 방식만 허용**
- Git 저장소에 비밀정보 커밋 시 **즉시 폐기 + 키 재발급**

#### 1.1.2 금지

- ❌ `application.yml`에 평문 비밀번호
- ❌ 테스트용이라는 이유로 키를 코드에 남기는 행위

#### 1.1.3 Secrets Rotation 정책 [MUST]

- **자동 로테이션 필수** (수동 로테이션 금지)
- 주기 기준 (2026년 권장):

| Secret 종류 | 로테이션 주기 | 구현 방법 추천 | 비고 |
|-------------|---------------|----------------|------|
| DB 비밀번호 | 30~90일 | AWS Secrets Manager + Lambda 자동 | RDS 연동 |
| API Key / 토큰 서명키 | 90일 이내 | HashiCorp Vault dynamic secrets | JWKS 로테이션 연계 |
| JWT signing key pair | 90~180일 | Vault 또는 KMS + 자동 재배포 | 무중단 로테이션 계획 필수 |
| Encryption key (KMS) | AWS-managed: 자동 / Customer-managed: 90~365일 | 자동 활성화 | 최소 1회 로테이션 증빙 |

- **무중단 로테이션 계획 문서화 필수** (blue-green, canary 등)
- **Dynamic Secrets 우선**: Vault DB 엔진 → 사용 시점에 생성 → TTL 만료 자동 폐기
- **감사 로그**: 모든 rotation 이벤트 CloudTrail/Vault audit 로그 저장

#### 1.1.3.1 Secrets Rotation 실패 대응 및 Grace Period [MUST]

자동 로테이션은 보안상 필수적이나, 인프라 장애나 동기화 지연으로 인한 서비스 중단을 방지하기 위해 아래 전략을 반드시 포함한다.

**Grace Period (유예 기간) 설정**

- 새로운 Secret이 생성된 후 **최소 1시간~24시간** 동안은 이전(Previous) Secret도 유효 상태를 유지해야 한다.
- AWS Secrets Manager의 경우 **AWSPREVIOUS** 스테이징 레이블을 활용하여 애플리케이션이 새 키로 갱신하기 전까지 구 키로 접근할 수 있도록 보장한다.

**재시도 및 지수 백오프 (Exponential Backoff)**

- 애플리케이션은 Secret 조회 실패 시 즉시 에러를 내지 않고, **지수 백오프 전략**을 사용하여 재시도한다.

**Rotation Health Check**

- 로테이션 람다(Lambda) 또는 스크립트는 키 변경 후, **실제 대상(DB 등)에 신규 키로 접속 테스트**를 수행한 후에만 최종 확정(Finalize)한다.

**긴급 롤백(Rollback) 절차**

- 로테이션 직후 에러율(4xx, 5xx) 급증 시, 관리자의 개입 없이 또는 즉각적인 승인을 통해 **이전 버전의 Secret으로 복구**하는 매뉴얼을 보유해야 한다.

#### 1.1.4 Secrets Rotation 금지 & 권장

- ❌ 장기 정적 secrets (180일 초과)
- ❌ 로테이션 후 애플리케이션 재시작 의존
- ✅ Vault dynamic secrets 또는 AWS Secrets Manager rotation lambda 사용

### 1.2 인증·인가 기본 규칙 (OWASP A01:2025 Broken Access Control)

> **A01:2025 Broken Access Control** — 권한 없는 접근 허용. IDOR, 강제 브라우징, 권한 상승, CORS 오설정, **SSRF** 포함. 여전히 가장 흔하고 심각한 리스크.

#### 1.2.1 주요 원칙

- **인증(Authentication)과 인가(Authorization) 로직 분리**
- 모든 보호 리소스는 기본적으로 차단(**deny-by-default**)
- 역할(Role) 또는 권한(Permission) 없는 API 노출 금지

#### 1.2.2 강제사항

- 인증 실패 / 인가 실패 → 명확한 HTTP Status 사용
  - 인증 실패: `401 Unauthorized`
  - 권한 부족: `403 Forbidden`

#### 1.2.3 방어 대상

- ❌ IDOR(직접 객체 참조): 리소스 ID 검증 및 소유권 확인 필수
- ❌ 강제 브라우징: 모든 경로별 인가 검사
- ❌ 권한 상승: 수직/수평 권한 검증
- ❌ CORS 오설정: 허용 오리진 최소화, `*` 금지
- ❌ SSRF: 외부 URL 입력 검증, 내부망 호출 제한

#### 1.2.4 인증·인가 테스트 강제

- 모든 보호 API는 다음 테스트 중 **최소 1개 이상 필수**
  - 인증되지 않은 요청 → `401`
  - 권한 없는 요청 → `403`
- Controller / Security Filter 테스트에서 검증한다.
- **인증·인가 테스트 없는 신규 API는 코드 리뷰에서 반려한다.**

> Broken Access Control은 테스트 부재에서 발생. 설정은 맞는데 엔드포인트 하나 열려 있는 사고 방지.

### 1.3 입력값 검증 (OWASP A05:2025 Injection)

> **A05:2025 Injection** — SQL, NoSQL, OS 명령어, LDAP 등 인젝션. 입력 검증 미흡 시 발생.

#### 1.3.1 검증 요구

- **모든 외부 입력은 신뢰하지 않는다**
- Controller 단에서 형식 검증(`@Valid` 등)
- 비즈니스 규칙 검증은 Service 계층

#### 1.3.2 금지

- ❌ 클라이언트 검증만 믿는 행위
- ❌ 검증 없는 `Map`, `Object` 그대로 사용

#### 1.3.3 인젝션 방지

- **SQL/JPQL**: 파라미터 바인딩·준비문(Prepared Statement)만 사용, 문자열 concat 금지
- **NoSQL**: 입력 필터링, 연산자 인젝션 검증
- **OS 명령어**: 외부 명령 실행 금지, 필요 시 화이트리스트만 허용
- **LDAP/XPath**: 이스케이프 처리 및 파라미터화

### 1.4 로그 보안 (OWASP A09:2025 Security Logging and Monitoring Failures)

> **A09:2025 Security Logging & Monitoring** — 로그 누락·조작, 모니터링 미흡 → 침해 탐지 지연.

#### 1.4.1 로그 관련 규칙

- 로그에 비밀번호, 토큰, 개인정보 출력 금지
- 에러 로그에 Stack Trace 그대로 사용자에게 반환 금지
- 운영 환경에서 DEBUG 로그 금지

#### 1.4.2 모니터링 필수

- 인증 실패·권한 부족 시도 로깅
- 민감 작업(비밀번호 변경, 역할 변경 등) 감사 로그
- 로그 위변조 방지(무결성 검증 고려)

#### 1.4.3 로깅 기술 규칙 (SLF4J / Logback)

> Spring Boot 기본: SLF4J + Logback. [로그 레벨 결정 흐름](https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels) 참고.

##### 1.4.3.1 Logger 사용

- **SLF4J** 사용 (`org.slf4j.Logger`)
- `LoggerFactory.getLogger(MyClass.class)` 또는 Lombok `@Slf4j`
- `static final` Logger 선언 (재생성 방지)

##### 1.4.3.2 파라미터화 로깅 (Parameterized Logging)

- **`{}` placeholder** 사용, 문자열 concat 금지
- 이유: 로그 레벨 비활성화 시 불필요한 문자열 연산 방지

```java
// ✅ 권장
log.info("요청 처리: userId={}, action={}", userId, action);

// ❌ 금지
log.info("요청 처리: userId=" + userId + ", action=" + action);
```

##### 1.4.3.3 로그 레벨 정의 및 사용 기준

| 레벨 | 심각도 | 사용 시점 | local/dev | prod |
|------|--------|-----------|-----------|------|
| TRACE | 최저 | 매우 상세 내부 추적 (거의 사용 금지) | OFF | OFF |
| DEBUG | 낮음 | 개발/테스트용 상세 정보 (SQL 바인딩, 객체 상태) | DEBUG | OFF |
| INFO | 보통 | 서버 기동/종료, 주요 비즈니스 이벤트 성공 | INFO | INFO |
| WARN | 높음 | 비정상이지만 계속 진행 가능 (재시도, 대체 경로) | INFO | WARN |
| ERROR | 매우 높음 | 요청/기능 실패, 시스템 전체는 동작 | INFO | ERROR |

- **개발/로컬**: DEBUG 이상 기본 출력
- **운영**: INFO 이상 기본 출력, WARN/ERROR는 모니터링 대상
- 운영 중 일시 디버깅: 특정 패키지만 DEBUG로 동적 변경 (Actuator 등)

##### 1.4.3.4 로그 구성 문법 및 구조화 (Structured Logging)

- **JSON 형식** 권장 (ELK, Loki, CloudWatch 파싱 용이)
- **MDC** 활용: `requestId`, `userId`, `traceId` 등 요청 추적 필드 포함
- 예외 시 `exception` 필드에 stacktrace (운영에서는 3줄 제한 옵션 고려)

```json
{
  "timestamp": "2026-02-04T17:38:45.123+09:00",
  "level": "INFO",
  "logger": "com.example.service.OrderService",
  "requestId": "req-abc123",
  "message": "Order created successfully",
  "service": "order-api",
  "env": "prod"
}
```

##### 1.4.3.5 로그 파일 저장 및 관리 정책

| 항목 | local/dev | staging | prod |
|------|-----------|---------|------|
| 출력 | 콘솔 + 파일 | 콘솔 + 파일 + 중앙 수집 | 파일 + 중앙 수집 |
| 경로 | `./logs/` | `/app/logs/` | `/var/log/{service}/` |
| 파일명 | `app.log` | `app-%d{yyyy-MM-dd}.log` | `app-%d{yyyy-MM-dd-HH}.%i.log.gz` |
| 롤링 | 10MB 또는 일단위 | 일단위 + 50MB | 시간단위(1h) + 100MB |
| 압축 | 없음 | gzip | gzip |
| 보관 기간 | 7일 | 30일 | 90일(Hot) + 1년(Cold) |
| 최대 파일 수 | 10개 | 30개 | 720개(1h×30일) |

- Logback `RollingFileAppender` 또는 `logrotate` 사용
- 중앙 수집(EFK, Loki 등) 도입 시 파일 보관 3~7일, 나머지는 중앙 저장소 관리
- **디스크 가용성**: 로그 수집기(Loki, ELK 등)로의 전송이 확인된 로그 파일은 **로컬 서버에서 24시간 이내에 삭제**한다. (디스크 I/O·비용 절감)

##### 1.4.3.6 환경별 로그 출력 제어

- `application-{profile}.yml` 또는 `logback-spring.xml`로 프로파일별 설정
- local/dev: `com.example: DEBUG`
- prod: `root: INFO`, `com.example.repository: WARN` (SQL 로그 최소화)

##### 1.4.3.7 금지 사항 (Do Not)

- ❌ PII, 토큰, 비밀번호, 카드번호 등 민감정보 로그 출력
- ❌ 무의미한 반복 로그 (매 요청 "health check ok" 등)
- ❌ 운영 환경 DEBUG/TRACE 기본 활성화
- ❌ `System.out` / `System.err` 사용
- ❌ 예외 catch 후 로그 없이 진행

##### 1.4.3.8 모니터링 연계

- ERROR 이상 → 즉시 알림 (Slack, PagerDuty 등)
- WARN 누적 시 주간 리포트
- 로그 기반 메트릭(에러율, 응답시간) → Prometheus + Grafana 연동 권장

### 1.5 인증·통신 보안 (상세)

> 상세 흐름 및 구현 방법: `doc/SECURITY_AUTH.md` 참고

#### 1.5.1 HTTPS 필수 (CIA 기밀성)

- 인증 관련 통신은 HTTPS(SSL) 환경에서만 동작하도록 전제
- 운영: `server.ssl` 및 Security에서 `requiresSecure()` 적용, **HSTS Preload** 권장

#### 1.5.2 데이터 기밀성 (OWASP A04:2025 Cryptographic Failures)

> **A04:2025 Cryptographic Failures** — 약한 알고리즘, 키 관리 미흡, 평문 전송, 인증서 검증 누락 등.

- 민감 데이터(비밀번호, 개인정보 등)는 하이브리드 암호화(RSA+AES-GCM) 방식 적용
  - 서버: RSA 공개키 제공(`GET /api/public-key`)
  - 클라이언트: 랜덤 AES-256-GCM 키 생성 및 데이터 암호화
    - AES키는 서버의 RSA 공개키로 암호화
    - 전송시: `{encryptedKey, iv, encryptedData}`
  - 서버: RSA 개인키로 AES키 복호화 → AES-GCM으로 데이터 복호화

**예외(성능 고려)**: 고빈도 트래픽 API(대량 개인정보 조회 등)에서 CPU 부하·응답 속도 저하가 우려될 경우, **내부망 보안이 보장된 구간**에 한해 암호화 범위 조정을 검토할 수 있다. 기술 리더 승인 및 위험·대안 문서화 필수.

#### 1.5.2.1 암호화 금지·권장

- ❌ MD5, SHA1, DES, 3DES 등 약한 알고리즘 금지
- ❌ 평문 전송 금지(HTTPS 위에서만 전송)
- ✅ 키 관리: KMS, Vault 등 전용 저장소 사용
- ✅ TLS: 인증서 검증 필수, 자가 서명 운영 환경 사용 금지

#### 1.5.3 무결성 (OWASP A08:2025 Software and Data Integrity Failures)

> **A08:2025 Software & Data Integrity** — CI/CD 파이프라인, 자동 업데이트, deserialization 등 무결성 검증 미흡.

- AES-GCM의 인증 태그를 활용한 위변조 검증 필수
- CI/CD: 아티팩트 서명·검증, 빌드 파이프라인 무결성 확인
- Deserialization: 신뢰할 수 없는 입력 역직렬화 금지

#### 1.5.4 가용성

- DoS 방어: 로그인, 회원가입, 토큰갱신 등에 rate-limiting 적용
- 회원가입: CAPTCHA 권장
- 예외/에러메시지는 정보 최소화(정보 유출 방지)

#### 1.5.5 OWASP Top 10 2025 준수

- OWASP Top 10 2025 및 Authentication Cheat Sheet, API Security Top 10 등 보안 가이드라인 준수

#### 1.5.6 추가 보안 조치 (OWASP A07:2025 Authentication Failures)

> **A07:2025 Authentication Failures** — 인증 우회, 크리덴셜 스터핑, 세션 고정, 브루트포스 등.

- 비밀번호: BCrypt 또는 Argon2id 해싱(`Spring Security PasswordEncoder`)
- JWT 처리: **섹션 6.1 JWT 핵심 Rule** 준수 (검증·alg allow-list·iss/aud·유효기간·저장·Revocation)
  - Access Token: 수명 15분 이하
  - Refresh Token: Redis 저장, 전달은 HttpOnly+Secure+SameSite=Strict 쿠키(웹) 또는 모바일 secure storage 권장
- CSRF/XSS 방어: React/Spring에서 이중 방어
- CORS: 운영 환경에서는 허용 오리진 최소화
- 입력값 검증, 출력 인코딩 철저 적용
- 브루트포스 방지: 로그인 실패 횟수 제한(계정 잠금 등)
- Flutter: `flutter_secure_storage` 또는 hive + encryption 권장

#### 1.5.7 보안 취약점 대응

- 취약점 발견 시 반드시
  - 사유 명확화/문서화
  - 위험 설명, 수정방안 기록 및 코드 반영

### 1.6 보안 설정 (OWASP A02:2025 Security Misconfiguration)

> **A02:2025 Security Misconfiguration** — 기본값 노출, 불필요 포트 오픈, 클라우드 버킷 공개, 에러 메시지 정보 유출 등. 2021 대비 #5 → #2 급상승.

#### 1.6.1 필수 조치

- ❌ 기본 계정·비밀번호·경로 그대로 사용 금지
- ❌ 불필요한 포트·서비스·디버그 엔드포인트 노출 금지
- ❌ 클라우드 스토리지(S3 등) 공개 설정 금지
- ❌ 에러 메시지에 스택 트레이스·내부 경로·버전 정보 반환 금지
- ✅ 보안 헤더 설정(HSTS, X-Content-Type-Options, CSP 등)
- ✅ 불필요한 HTTP Method 비활성화

### 1.7 소프트웨어 공급망 (OWASP A03:2025 Software Supply Chain Failures)

> **A03:2025 Software Supply Chain** — 의존성·빌드·배포 체인 전체 취약점. Log4j류, SolarWinds류 공급망 공격 포함. 2025 신규 카테고리.

#### 1.7.1 필수 조치

- **의존성**: `dependency-check`, Dependabot 등으로 CVE 모니터링
- **의존성 버전**: 취약 버전 사용 금지, 정기 점검
- **빌드**: 신뢰할 수 있는 아티팩트 저장소만 사용, 서명 검증
- **배포**: 파이프라인 자격 증명 보호, 최소 권한 원칙

#### 1.7.2 SBOM(소프트웨어 자재 명세서) 자동화

- **CI 빌드 단계**에서 **CycloneDX** 또는 **SPDX** 형식의 SBOM 파일을 **자동 생성**하고 아카이징한다.
- 수동 SBOM 관리는 불가능에 가깝다. Gradle 플러그인(`cyclonedx-gradle-plugin` 등) 또는 Maven 플러그인으로 파이프라인에 통합한다.

### 1.8 AI 모델 및 API 보안 (2026 신규 권고)

LLM API 호출 등 AI 연동이 포함될 경우 아래 규칙을 적용한다.

#### 1.8.1 Prompt 보안

- **AI 모델에 전달되는 Prompt에 PII(개인정보) 및 비밀정보 포함 금지**
- 민감 데이터는 익명화·가명화 후 전달하거나, 별도 보안 구간에서만 처리

#### 1.8.2 AI 생성 코드 도입

- **AI 생성 코드 도입 시 반드시 수동 보안 리뷰 필수**
- 자동 생성·복사된 코드는 취약점 검증 없이 프로덕션에 반영 금지

### 1.9 Rate Limiting & Throttling [MUST]

모든 공개 API에 Rate Limiting 적용 (DoS, 브루트포스, 비용 폭증 방어)

#### 1.9.1 알고리즘 및 구현

- **알고리즘**: Token Bucket (Bucket4j 추천) 또는 Sliding Window (Resilience4j)
- **분산 환경 필수**: Redis 또는 Hazelcast 백엔드 사용 (Bucket4j-redis 확장)
- **Fallback / Backpressure**: 초과 시 `429 Too Many Requests` + `Retry-After` 헤더 반환
- **예외 처리**: GlobalExceptionHandler에서 429 → ErrorResponse 통일

#### 1.9.2 대상별 제한 기준 (운영 기본값)

| 대상 | 제한 예시 | 구현 라이브러리 | 비고 |
|------|-----------|-----------------|------|
| 로그인/인증 | 5~10 req / 1분 (IP+계정) | Bucket4j + Redis | 계정 잠금 연계 |
| 토큰 발급/갱신 | 20 req / 5분 | Bucket4j | Refresh 토큰 남용 방지 |
| 일반 API (읽기) | 300~1000 req / 분 (userId) | Resilience4j RateLimiter | 글로벌 vs 사용자별 |
| 민감 API (쓰기) | 50 req / 분 | Bucket4j | 데이터 변경 작업 |
| 비인증 API | 100 req / 분 (IP 기준) | Spring Cloud Gateway | 봇/크롤러 방어 |

#### 1.9.3 구현 예시 (Bucket4j)

```java
@Bean
public Bucket loginBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
        .build();
}
```

#### 1.9.4 금지

- ❌ 무제한 API
- ❌ 클라이언트 측 rate limit만 의존

---

## 2. 기능(Function) RULE — 일관성과 예측 가능성 [MUST]

### 2.1 API 설계 규칙 (OWASP A06:2025 Insecure Design)

> **A06:2025 Insecure Design** — 설계 단계 보안 결함. 위협 모델링 부족, 안전 설계 패턴 미적용.

#### 2.1.1 설계 원칙

- 모든 API는 **명확한 책임 단위**
- 위협 모델링: 신규 기능·API 설계 시 보안 위협 식별
- 하나의 API에서 아래 항목 혼합 금지:
  - 인증
  - 검증
  - 비즈니스 처리
  - 외부 연동

#### 2.1.2 강제

- HTTP Method 의미 준수
  - 조회: `GET`
  - 생성: `POST`
  - 수정: `PUT` / `PATCH`
  - 삭제: `DELETE`

#### 2.1.3 내부/관리자 API 분리 원칙

- 내부 API, Admin API는 외부 API와 **URL·보안 정책 분리**
  - 예: `/api/internal/**`, `/api/admin/**`
- Admin API는 **IP allow-list** 또는 **별도 인증 체계** 필수

> 실제 사고 다수: "운영용 API를 외부에서 호출 가능"

### 2.2 예외 처리 규칙 (OWASP A10:2025 Mishandling of Exceptional Conditions)

> **A10:2025 Mishandling of Exceptional Conditions** — 예외·에러 처리 미흡. 스택 트레이스 노출, DoS 유발, 상태 불일치, 민감정보 유출 등. 2025 신규 카테고리.

#### 2.2.1 예외 처리 원칙

- **예외는 의도적으로 처리**
- 모든 예외는 의미 있는 에러 코드 또는 메시지 반환

#### 2.2.2 금지

- ❌ `try-catch`로 예외 무시
- ❌ `RuntimeException` 남발
- ❌ 에러 발생 시 `200 OK` 반환
- ❌ 스택 트레이스·내부 경로를 사용자 응답에 포함
- ❌ 예외 처리 미흡으로 인한 DoS·상태 불일치 허용

#### 2.2.3 공통 예외 체계 사용 (필수)

- 비즈니스 로직 예외는 반드시 공통 예외 체계(`BusinessException`, `ErrorCode`) 사용
- 예외 발생 시
  - 코드/메시지/HTTP 상태를 `ErrorCode enum`에 정의
  - 모든 예외는 `GlobalExceptionHandler`에서 잡아 일관된 `ErrorResponse`로 변환

#### 2.2.4 금지된 예외 종류 (비즈니스 로직 내 사용금지)

- ❌ `IllegalArgumentException`, `IllegalStateException` 직접 사용 금지
- ❌ `RuntimeException`을 직접 상속하는 커스텀 예외(공통 체계 미적용)
- ❌ `ErrorCode`와 무관한 기타 예외

#### 2.2.5 ErrorCode 정의 가이드

- 신규 비즈니스 에러는 `ErrorCode`에 추가 후 공통 예외로만 사용
- `ErrorCode`는 enum에 코드/HTTP 상태/메시지 필수 지정
- 필요 시 도메인별 예외 클래스에 정적 팩토리 메서드 제공

#### 2.2.6 Checked Exception 사용 금지

- **비즈니스 로직에서 Checked Exception 사용 금지**
- 모든 도메인 예외는 공통 **Runtime 기반 예외 체계**로 통합

> 트랜잭션 롤백 누락, 호출부 예외 삼킴 사고 방지.

### 2.3 트랜잭션 규칙

#### 2.3.1 트랜잭션 경계

- 트랜잭션 경계는 **Service 계층**
- 읽기 전용 조회는 `readOnly = true`
- 외부 API 호출을 트랜잭션 내부에 두지 않는다

#### 2.3.2 트랜잭션과 이벤트

- **트랜잭션 내부에서 외부 이벤트 발행 금지**
- 반드시 **트랜잭션 커밋 이후** 발행
  - 예: `@TransactionalEventListener(phase = AFTER_COMMIT)`

> 롤백 후 메시지 발행 → 데이터 불일치 사고 다발

### 2.4 상태 관리

#### 2.4.1 상태 관리 원칙

- 서버는 가능한 한 **Stateless**
- **인증 우선순위**: 인증은 기본적으로 **JWT 기반 Stateless 방식**을 원칙으로 한다. 부득이하게 세션을 사용할 경우(하이브리드: Web 세션 + App 토큰 등)에만 스토리지를 분리·명시한다.
- 세션 사용 시:
  - 세션 스토리지 명시
  - 확장성 고려(Sticky session 전제 금지)

---

## 3. 기술(Technical) 제한 RULE — 프로젝트 생존성 [SHOULD]

### 3.1 아키텍처 계층 규칙

#### 3.1.1 계층 의존성

- 계층 간 의존성 **단방향 유지**

```text
Controller → Service → Domain/Repository
```

#### 3.1.2 금지

- ❌ Controller → Repository 직접 호출
- ❌ Domain이 Infra 기술(Spring, JPA 등)에 종속

### 3.2 프레임워크 의존성 제한

#### 3.2.1 의존성 제한

- 특정 프레임워크 기능에 과도한 종속 금지
- 프레임워크 교체 불가능한 구조 금지

#### 3.2.2 예시

- ❌ 비즈니스 로직이 `@Controller`, `@Entity`에 직접 묶여 있음

### 3.3 ORM 사용 규칙

#### 3.3.1 ORM 기본 원칙

- **N+1 문제 인지하고 대응**
- `EAGER` 기본 금지, **`LAZY` 기본**
- 엔티티를 API 응답 객체로 직접 반환 금지

#### 3.3.2 QueryDSL 개발 규칙

##### 3.3.2.1 JPAQueryFactory 주입

- `QueryDslConfig`에서 Bean으로 등록된 `JPAQueryFactory`를 사용한다.
- 서비스/레포지토리 레이어에서는 `@RequiredArgsConstructor`를 활용하여 주입한다.

##### 3.3.2.2 Q클래스 사용

- QueryDSL이 자동 생성한 Q클래스만을 사용한다.
  - 예시: `QReservation.reservation`, `QMember.member`
- 직접 Q객체를 생성해서 사용하지 않는다.

##### 3.3.2.3 페치 조인(fetchJoin) 활용

- N+1 문제 방지를 위해 반드시 `fetchJoin()`을 필요한 쿼리에 추가한다.

```java
queryFactory.selectFrom(reservation)
  .join(reservation.member, member).fetchJoin()
```

### 3.4 동기 / 비동기 통신 규칙

#### 3.4.1 통신 원칙

- 외부 시스템 호출은 **Timeout 필수**
- **Retry 정책 명시**
- Circuit Breaker 없는 무한 재시도 금지

### 3.5 AOP 개발 규칙

AOP는 **횡단 관심사(Cross-Cutting Concern)** 전용이며, 비즈니스 로직·도메인 규칙·상태 변경·핵심 흐름 제어에는 사용하지 않는다.

#### 3.5.1 허용 대상 — 횡단 관심사만

- **허용**: 로깅(Access Log, Audit Log), 트랜잭션 경계 제어, 보안/권한 검사, 성능 측정·메트릭, 공통 예외 처리 보조, 분산 트레이싱
- **금지**: 비즈니스 로직, 도메인 규칙, 상태 변경 로직, 핵심 흐름 제어

**Rule**: AOP 안에는 **"업무 규칙을 판단하는 코드"**가 절대 들어가면 안 된다.

**이유**: 흐름이 코드 외부에서 변경됨, 디버깅 불가능, 유지보수 비용 급증.

#### 3.5.2 보조 역할 — 필수 요소 금지

- ❌ **나쁜 예**: AOP가 없으면 서비스가 정상 동작하지 않음, AOP 내부에서 필수 데이터 세팅
- ✅ **좋은 예**: AOP 제거해도 시스템은 동작, 단지 "추가 정보"만 사라짐

**Rule**: **AOP는 제거해도 시스템이 정상 동작해야 한다.**

#### 3.5.3 Pointcut — 명시적·좁게 정의

- ❌ **금지**: `execution(* com.app..*(..))` (패키지 전체)
- ✅ **권장**: `execution(* com.app.order.application..*(..))`, `@annotation(TrackExecutionTime)` (Annotation 기반 우선)

**Rule**: **패키지 전체 포인트컷 금지, Annotation 기반 우선 사용.**

**이유**: 의도하지 않은 곳에 적용, 성능 문제, 신규 클래스 추가 시 부작용.

#### 3.5.4 예외 처리 — 비즈니스 예외 삼키기 금지

- ❌ **금지**: AOP 내부에서 예외 catch 후 `return null` 또는 예외 무시
- ✅ **허용**: 예외 로깅 후 **재throw** (`throw e`)

**Rule**: **AOP는 예외를 기록만 하고, 판단하거나 변환하지 않는다.** (Global Exception Handler가 담당)

#### 3.5.5 트랜잭션 AOP — Service 계층만

- **허용 위치**: Application Service, UseCase Layer
- **금지 위치**: Domain Model, Repository 구현체, Controller

**Rule**: **`@Transactional`은 Service 계층에만 선언한다.**

**이유**: 트랜잭션 범위 추적 불가, Lazy Loading 오류, 테스트 어려움.

#### 3.5.6 상태 변경 금지

- ❌ **금지**: Entity 수정, Request 객체 변경, ThreadLocal 값 임의 조작
- ✅ **허용**: 읽기 전용 접근, 별도 컨텍스트 객체에 기록

**Rule**: **AOP는 관찰자(observer) 역할만 수행한다.**

#### 3.5.7 순서(Order) 명시

- 다중 AOP 사용 시 **`@Order` 필수** (예: Security → Transaction → Logging)

**Rule**: **다중 AOP 사용 시 `@Order` 필수.**

**이유**: 트랜잭션 전에 로그? 보안 이후 트랜잭션? 순서 불명확 시 버그 재현 불가.

#### 3.5.8 성능 민감 영역 — 사전 검증

- **대상**: 대량 반복 호출, 배치, 스트리밍 처리

**Rule**: **반복 호출되는 로직에는 AOP 적용 전 성능 테스트 필수.**

#### 3.5.9 Self Invocation — 인지 및 해결 원칙

- **문제**: `this.internalMethod()` 호출 시 AOP 미적용

**Rule**: **동일 클래스 내부 호출에는 AOP가 적용되지 않음을 명확히 인지.**

**해결 원칙(강제)**: Self-invocation 해결을 위해 **Proxy를 직접 다루는 코드**(`AopContext.currentProxy()`, `ObjectProvider`로 Proxy 주입 등)는 **금지**한다. 반드시 **물리적인 클래스(빈) 분리**를 통해 해결한다.

**대안**: 별도 빈으로 분리, Application Service → Domain Service 분리.

#### 3.5.10 문서화 필수

- **필수 문서 항목**: 적용 대상(Pointcut), 목적, 실행 시점, 예외 처리 정책, 제거 시 영향도

**Rule**: **AOP 추가 시 README 또는 ADR 문서 작성 필수.**

---

#### AOP 원칙 요약 (1페이지)

| # | Rule |
| --- | ------ |
| 1 | AOP는 횡단 관심사 전용, 비즈니스 로직 금지 |
| 2 | AOP 제거해도 시스템 정상 동작 (보조 역할) |
| 3 | Pointcut 명시적·좁게, Annotation 기반 우선 |
| 4 | 예외 판단·변환 금지, 기록 후 재throw |
| 5 | 상태 변경 금지, 관찰자 역할만 |
| 6 | `@Transactional`은 Service 계층만 |
| 7 | 다중 AOP 시 `@Order` 필수 |
| 8 | 성능 민감 영역은 적용 전 성능 검증 |
| 9 | Self Invocation 미적용 인지, Proxy 직접 다루기 금지·빈 분리로 해결 |
| 10 | AOP 추가 시 문서화(Pointcut·목적·영향도) 필수 |

---

## 4. 품질(Quality) RULE — 유지보수 가능성 [MUST]

### 4.1 코드 규칙

#### 4.1.1 코드 작성

- 메서드 하나당 **책임 하나**
- 메서드 길이 과도 금지 (보통 **30~40줄 기준**)
- **매직 넘버, 문자열 상수화**

### 4.2 테스트 최소 기준

#### 4.2.1 테스트 원칙

- 비즈니스 로직에 대한 테스트 존재
- **테스트 없는 핵심 로직 배포 금지**
- 테스트는 외부 환경(DB, API)에 의존하지 않는다
- **슬라이스/통합 테스트 시 DB**: In-memory(H2) 또는 **Testcontainers**를 통한 격리된 환경만 사용한다. **실제 운영/개발 DB 연결은 금지**한다.

#### 4.2.1.1 테스트 결정성 보장

- 테스트 코드에서 `System.currentTimeMillis()`, `LocalDateTime.now()` **직접 사용 금지**
- `Clock`, `TimeProvider` 등으로 **추상화**
- 랜덤값은 **고정 시드** 또는 **Stub** 사용

> CI에서만 깨지는 테스트 원인 1순위

#### 4.2.2 테스트 코드 작성 규칙 (2025~2026 베스트 프랙티스)

> 모든 단위 테스트·슬라이스 테스트는 아래 규칙을 준수한다. 예외가 필요한 경우 기술 리더 승인 및 문서화를 거친다.

##### 4.2.2.1 기본 원칙

- **Given-When-Then 패턴 필수**: 모든 단위 테스트·슬라이스 테스트는 3단계 구조를 반드시 준수한다.
- **주석 구분**: 테스트 메서드 본문에 `// given`, `// when`, `// then` 주석으로 명시적으로 3단계를 구분한다.
- **AssertJ 사용**: 결과 검증에는 JUnit 기본 Assertions 대신 **AssertJ**를 사용한다.
- **Mock 라이브러리**: Java → **Mockito**, Kotlin → **MockK** 사용.
- **테스트 메서드 이름**: 행위 중심 + 결과 중심, **BDD 스타일** 권장.

##### 4.2.2.2 테스트 유형별 적용 범위

| 테스트 유형 | 사용 어노테이션 조합 | 목 객체 사용 | Given-When-Then 필수 | 추천 Assert | 비고 |
|-------------|----------------------|--------------|----------------------|-------------|------|
| 순수 단위 테스트 (Service, Util 등) | `@ExtendWith(MockitoExtension.class)` | Mockito / MockK | 필수 | AssertJ | Spring 컨텍스트 로드 X |
| Repository 슬라이스 테스트 | `@DataJpaTest` + `@AutoConfigureTestDatabase` | 필요 시 Mock | 필수 | AssertJ | H2 또는 Testcontainers만 사용 |
| Controller 슬라이스 테스트 | `@WebMvcTest` + `@MockBean` | 필수 (Service 등) | 필수 | AssertJ + MockMvc | |
| 전체 통합 테스트 | `@SpringBootTest` + `@AutoConfigureMockMvc` | 최소화 | 권장 (복잡할 경우 필수) | AssertJ | 느리므로 최소화 |

##### 4.2.2.3 테스트 메서드 이름 규칙 (강력 추천)

- `[메서드명]_[상황설명]_should[기대결과]` 또는 `should[기대결과]_when[상황]`

**예시 (Java)**

```java
findById_존재하는ID_주면_해당회원을반환한다
register_중복이메일이면_예외를던진다
```

- Kotlin: `snake_case` 또는 자연어 스타일 허용 (팀 결정)

##### 4.2.2.4 코드 구조 템플릿 (Java + JUnit 5 + Mockito + AssertJ)

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원 서비스 단위 테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("findById - 존재하는 ID로 조회하면 해당 회원을 반환한다")
    void findById_존재하는ID_주면_해당회원을반환한다() {
        // given
        Long memberId = 1L;
        Member expected = Member.builder()
                .id(memberId)
                .email("test@example.com")
                .nickname("테스트유저")
                .build();

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(expected));

        // when
        Member actual = memberService.findById(memberId);

        // then
        assertThat(actual)
                .isNotNull()
                .extracting("id", "email", "nickname")
                .containsExactly(memberId, "test@example.com", "테스트유저");
    }

    @Test
    @DisplayName("register - 이미 존재하는 이메일이면 예외를 던진다")
    void register_중복이메일이면_예외를던진다() {
        // given
        MemberCreateRequest request = MemberCreateRequest.builder()
                .email("duplicate@example.com")
                .build();

        given(memberRepository.existsByEmail(request.getEmail()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용중인 이메일입니다.");
    }
}
```

##### 4.2.2.5 Kotlin + MockK 템플릿 (선택)

```kotlin
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk

@ExtendWith(MockitoExtension::class)  // 또는 Kotest + MockK 조합
@DisplayName("회원 서비스")
class MemberServiceTest {

    private val memberRepository: MemberRepository = mockk()
    private val memberService = MemberService(memberRepository)

    @Test
    fun `이메일로 회원 조회 - 존재하면 회원 반환`() {
        // given
        val email = "test@example.com"
        val expected = Member(id = 1L, email = email, nickname = "테스트")

        every { memberRepository.findByEmail(email) } returns expected

        // when
        val actual = memberService.findByEmail(email)

        // then
        assertThat(actual).isEqualTo(expected)
    }
}
```

##### 4.2.2.6 추가 강제 규칙

- **주석 강제**: 모든 테스트에 `// given`, `// when`, `// then` 3줄 주석 필수 (가독성 극대화)
- **BDDMockito 권장**: `given(...).willReturn(...)` 형식 사용 (`when(...).thenReturn(...)` 대신)
- **AssertJ 체이닝**: `extracting()`, `hasFieldOrPropertyWithValue()`, `satisfies()` 등 적극 활용
- **@DisplayName 필수**: 테스트 클래스와 메서드 모두에 의미 있는 한글 설명 작성
- **예외 테스트**: `assertThatThrownBy()` 사용
- **단일 책임**: 한 테스트 메서드 = 한 시나리오

### 4.3 문서 규칙

#### 4.3.1 문서화 기준

- 공개 API → **Swagger/OpenAPI 필수**
- 설정 값 → README 또는 config 문서화
- ❌ "코드 보면 안다" 금지

#### 4.3.2 Task 문서 작성 구조 (`doc/TASK.md`)

> Task 문서(`doc/TASK.md`)를 **새로 작성**하거나 **Step을 추가**할 때 반드시 아래 구조를 준수한다.

##### 4.3.2.1 필수 필드 정의

| 필드 | 설명 |
| ------ | ------ |
| **Step Name** | 단계 이름 |
| **Step Goal** | 이 단계를 끝냈을 때 달성할 목표(한 문장) |
| **Input** | 이 단계에 필요한 입력(문서·코드·환경 등) |
| **Scope** | 포함/제외로 단계 범위 명시 |
| **Instructions** | 수행할 작업 목록 |
| **Output Format** | 산출물 형태·위치·형식 |
| **Constraints** | 지켜야 할 제약(RULE·기술 등) |
| **Done When** | 아래 조건이 충족되면 단계 완료로 간주 |
| **Duration** | 예상 소요일수 |
| **RULE Reference** | 참조할 RULE.md 섹션 |

##### 4.3.2.2 강제 사항

- 신규 Step 추가 시 위 **10개 필드를 모두 작성**한다.
- 기존 Step 수정 시 해당 필드가 있다면 내용을 **갱신**한다.
- 필드 누락 시 코드 리뷰에서 보완 요청 대상이 된다.

### 4.4 주석 규칙 (v1.0 — 2026.02)

> 주석은 Why를 설명한다. What은 코드로 표현한다.

#### 4.4.1 기본 원칙

- 주석은 **Why(이유)** 를 설명한다. What(무엇)은 코드로 표현한다.
- **한글 주석**을 기본으로 한다 (영어 허용하나 프로젝트 내 일관성 유지)
- 주석은 코드와 **동기화**되어야 한다.

#### 4.4.2 문서화 주석 (Javadoc)

- **public API**(클래스, 메서드, 인터페이스) → 필수
- Javadoc 형식 준수
- 구조: 한 줄 요약 → 빈 줄 → 상세 설명 → 태그 순서 (`@param` → `@return` → `@throws` → `@example`)

```java
/**
 * 회원 이메일로 조회한다.
 *
 * @param email 이메일 (unique)
 * @return 회원 Optional
 */
Optional<User> findByEmail(String email);
```

#### 4.4.3 구현 주석 (Inline / Block)

- **비직관적인 로직**에만 작성
- `//` 한 줄 주석 선호
- 블록 주석 `/* */` 사용 시 `*` 정렬 유지

#### 4.4.4 금지 사항

- ❌ 코드와 동일한 내용 반복 (예: `i++` // i를 1 증가시킴)
- ❌ 주석 박스(`*********`) 사용 금지
- ❌ 불필요한 주석으로 코드 가독성 저하 금지

#### 4.4.5 언어별 세부 규칙

- **Java/Kotlin** → Google Java Style Guide + Javadoc
- **JavaScript/TypeScript** → JSDoc + Airbnb 스타일
- **Python** → PEP 8 + Google Python Style Guide docstring

#### 4.4.6 린팅·검증

- Checkstyle, SpotBugs 등에 주석 규칙 플러그인 적용 예정

---

## 5. 운영(Operation) RULE — 사고 대응 [SHOULD]

### 5.1 설정 분리 (OWASP A02:2025 연계)

#### 5.1.1 분리 원칙

- 환경별 설정 분리(`local` / `dev` / `prod`)
- 운영 환경에서 개발용 설정 금지

### 5.2 장애 대비

#### 5.2.1 장애 대응

- 모든 외부 연동 실패 시 **Fallback 전략 정의**
- 장애 발생 시 **로그만 보고 원인 추적 가능**해야 함

### 5.3 긴급 비활성화 전략

- 핵심 기능은 **Feature Toggle** 또는 **Kill Switch** 고려
- 장애 시 **배포 없이 기능 차단** 가능해야 함

> 실제 운영 사고 시 "코드 고쳐서 배포"는 너무 늦음

---

## 6. 인증/토큰 관리 규칙 [MUST]

### 6.1 JWT (JSON Web Token) 핵심 Rule

JWT는 가장 많이 오용되는 인증 방식 중 하나이다. 아래 규칙은 **절대 위반 금지**이다.

#### 6.1.1 항상 모든 JWT 검증

- 내부 통신이든 외부 요청이든, JWT를 받는 **모든** 서비스/미들웨어에서 무조건 **서명·클레임 검증** 수행
- ❌ "이미 검증된 토큰"이라는 가정 금지

#### 6.1.2 alg 헤더 신뢰 금지 — allow-list로 제한

- ❌ **`alg: "none"`** 절대 허용 금지
- 허용 알고리즘은 **명시적 allow-list**로만 관리
  - 권장 우선순위: **EdDSA > ES256 > RS256**
- HS256은 가능하면 피하고, **비대칭키(공개키)** 사용 권장

#### 6.1.3 iss(issuer), aud(audience) 무조건 검증

- **iss**: 정확히 일치해야 함 (예: `https://auth.example.com`)
- **aud**: 리소스 서버의 식별자가 반드시 포함되어야 함
  - audience mismatch → **즉시 거부**

#### 6.1.4 짧은 유효기간 필수 (short-lived token)

- **Access Token**: 5~60분 이내 만료 (본 프로젝트: 15분 이하 유지)
- **Refresh Token**: 1일~30일, 가능하면 revocation 지원
- **exp, nbf, iat** 모두 검증
- Clock skew는 **최대 30초** 허용

#### 6.1.5 민감한 정보 절대 payload에 넣지 말 것

- 이메일, 전화번호, 권한 목록, 프레임워크 정보 등 → **Base64 디코딩으로 노출됨**
- 민감 데이터가 필요하면 **UserInfo 엔드포인트** 또는 별도 API 호출로 대체

#### 6.1.6 저장 방식

- **웹**: HttpOnly + Secure + SameSite=Strict/Lax **쿠키** 사용
  - ❌ localStorage / sessionStorage 절대 금지 (XSS 취약)
- **모바일**: Secure Storage (Keychain, Keystore, `flutter_secure_storage`) 사용
- Bearer 토큰은 누구나 제시하면 받아들이는 구조 → **PoP(Proof of Possession)** 고려

#### 6.1.7 Revocation 지원 필수

- **jti (JWT ID)** 필수 포함
- 블랙리스트(Redis 등) 또는 refresh token revocation 메커니즘 구현
- 로그아웃/토큰 탈취 시 **즉시 무효화** 가능해야 함

#### 6.1.8 키 관리 (JWT)

- **JWKS** 엔드포인트에서 공개키 동적 다운로드 → 키 로테이션 자동 지원
- 대칭키(HMAC)는 하드코딩/코드에 넣지 말고 **Secret Manager** 사용

---

### 6.2 RSA (비대칭 암호화) 및 키 관리 Rule

RSA는 JWT 서명(RS256)이나 데이터 암호화에 자주 사용된다.

#### 6.2.1 키 길이 최소 기준 (2025년 기준)

- **RSA**: 최소 2048bit (권장 **3072bit 이상**)
- **ECDSA**: 최소 P-256 (secp256r1), 권장 **Ed25519** 또는 P-384

#### 6.2.2 개인키(Private Key) 절대 노출 금지

- ❌ 코드, Git, 로그, 환경변수에 하드코딩 금지
- ✅ HSM, Vault, AWS KMS, GCP Secret Manager 등에 저장
- 메모리에도 장기 보관 금지 → **사용 후 즉시 제거**

#### 6.2.3 단일 목적 원칙 (One key, one purpose)

- **서명용 키 ≠ 암호화용 키 ≠ 키 암호화용 키(KEK)**

#### 6.2.4 키 생성 및 배포

- FIPS 140-2/3 준수 모듈에서 생성
- 공개키는 **JWKS** 또는 신뢰할 수 있는 CA를 통해 배포
- 키 로테이션 주기 명확히 정의 (최소 1년 이내, 이상적으론 **90일**)

#### 6.2.5 완전 순방향 비밀성 (PFS) 고려

- 세션 키는 **ephemeral(일회성)** 으로 생성

---

### 6.3 인증(Authentication) & 인가(Authorization) — CIA 관점

CIA Triad를 개발 관점에서 적용한 필수 규칙이다.

| CIA 요소 | 핵심 Rule | 이유 / 위협 방어 |
| -------- | --------- | ---------------- |
| **Confidentiality (기밀성)** | HTTPS 강제 (HSTS Preload 포함), 민감 데이터는 JWT에 넣지 말고 별도 암호화 전송, PII 최소화 + Pseudonymous ID 사용 | 데이터 유출, MITM, XSS |
| **Integrity (무결성)** | 모든 JWT 서명 검증, RSA/ECDSA 등 강력 알고리즘, 메시지 무결성 검증 (MAC, 서명) | 위·변조 공격 |
| **Availability (가용성)** | Rate Limiting + DDoS 방어, 토큰 블랙리스트가 성능 저하시키지 않도록 설계 (Redis 등), 키 로테이션 시 무중단 처리 | DoS, 서비스 거부 |

---

### 6.4 인가(Authorization) 추가 Rule — OAuth 2.0 / OIDC 기반

#### 6.4.1 최소 권한 원칙 (Least Privilege)

- Scope는 기능별로 세분화 + **incremental authorization** 사용

#### 6.4.2 Authorization Code Flow + PKCE 필수 (SPA/모바일)

- ❌ **Implicit Flow**, **Password Grant** 절대 사용 금지

#### 6.4.3 Redirect URI 엄격 검증

- **정확한 URI allow-list** (와일드카드 남용 금지)
- open redirector 방지

#### 6.4.4 State / Nonce 파라미터 필수

- **State**: CSRF 방어
- **Nonce**: Replay 공격 방어

---

### 6.5 토큰 무효화 및 세션 관리 (요약)

- Access Token: 유효기간 **15분 이하**
- Refresh Token: Redis 저장, 웹은 HttpOnly+Secure+SameSite=Strict 쿠키, 모바일은 secure storage
- 토큰 무효화·세션 관리의 용이성을 최우선으로 설계 (6.1.7 Revocation 참고)

---

## 7. 플랫폼별 구현 가이드 (Platform-specific Implementation)

### 7.1 Spring Boot (Back-end)

백엔드에서는 **보안 규정**과 **데이터 무결성**에 집중한다.

#### 7.1.1 [보안] 환경 변수 관리

**Rule**: DB 비밀번호 등 민감 정보는 `application.yml`에 직접 노출하지 않고 **환경 변수**를 사용한다.

```yaml
# Good Case
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

#### 7.1.2 [기능] 전역 에러 핸들러 (Global Exception Handler)

**Rule**: 모든 예외는 **공통된 객체 형식**으로 반환하여 클라이언트의 처리를 돕는다.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse response = new ErrorResponse(e.getErrorCode(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
```

---

### 7.2 React (Web Front-end)

프론트엔드에서는 **입력값 검증**과 **인증 토큰 관리**가 핵심이다.

#### 7.2.1 [보안] XSS 방지 및 상태 관리

**Rule**: 사용자 입력값을 렌더링할 때 `dangerouslySetInnerHTML` 사용을 **금지**하며, API 통신 시 토큰은 **메모리**나 **HttpOnly 쿠키**에 저장한다.

```javascript
// Good Case: 입력값 검증 라이브러리(Zod) 사용
const loginSchema = z.object({
  email: z.string().email("이메일 형식이 아닙니다."),
  password: z.string().min(8, "8자 이상 입력해주세요."),
});

const onSubmit = (data) => {
  const result = loginSchema.safeParse(data);
  if (!result.success) {
    alert(result.error.issues[0].message);
    return;
  }
  // API 호출 로직...
};
```

#### 7.2.2 [기술 제한] API 인스턴스 공통화

**Rule**: 모든 API 호출은 **공통 Axios 인스턴스**를 사용하며, 헤더에 토큰을 자동으로 주입한다.

```javascript
const api = axios.create({ baseURL: process.env.REACT_APP_API_URL });

api.interceptors.request.use((config) => {
  const token = getAccessToken(); // 메모리 등에서 토큰 획득
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

---

### 7.3 Flutter (Mobile App)

모바일 환경에서는 **데이터 보안**과 **비동기 처리**가 중요하다.

#### 7.3.1 [보안] 보안 저장소 사용

**Rule**: 민감한 데이터(자동 로그인 토큰 등)는 `SharedPreferences`가 아닌 **`flutter_secure_storage`**를 사용한다.

```dart
// Good Case
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

final storage = FlutterSecureStorage();

// 데이터 저장
await storage.write(key: 'jwt_token', value: token);

// 데이터 읽기
String? value = await storage.read(key: 'jwt_token');
```

#### 7.3.2 [기술 제한] 에러 바운더리 및 로깅

**Rule**: 비동기 작업 시 반드시 **try-catch**를 사용하며, UI 레벨에서 사용자에게 에러 상황을 알린다.

```dart
Future<void> fetchData() async {
  try {
    final response = await apiService.get('/data');
    setState(() => _data = response);
  } catch (e, stackTrace) {
    // 개발 단계에서는 로깅, 사용자에게는 친절한 메시지
    log('Error fetching data: $e', stackTrace: stackTrace);
    showSnackBar('데이터를 불러오는 중 오류가 발생했습니다.');
  }
}
```

---

## 8. 프론트엔드 JavaScript 코딩 규칙 (2026 ver.)

> 현대 프론트엔드 팀(React, Vue, Next.js, Vite 등)이 따르는 현실적인 규칙. 프론트엔드 코드 작성 시 준수한다.

### 8.1 변수 & 상수 선언 규칙 (Variables & Constants)

| # | Rule | 비고 |
|---|------|------|
| 1 | **var는 절대 사용하지 않는다** (ES6 이후 완전 금지) | |
| 2 | 재할당이 필요 없는 모든 변수는 **const**로 선언한다. (기본 원칙: const > let) | |
| 3 | 재할당이 반드시 필요한 경우에만 **let**을 사용한다. (for 루프의 i, j 등은 let 허용, for...of/forEach 내부에서는 const 추천) | |
| 4 | const와 let은 **사용 직전에 선언**한다. (hoisting 문제 최소화) | |
| 5 | 같은 스코프 내에서 const를 let보다 위에 선언한다. (가독성) | |
| 6 | **전역 변수는 절대 사용하지 않는다.** (window.xxx, globalThis.xxx 직접 할당 금지. 필요 시 Context, Zustand, Redux, Jotai 등 상태 관리 도구 사용) | |
| 7 | **네이밍**: 변수/함수 → camelCase, 상수(진짜 바뀌지 않는 값) → UPPER_SNAKE_CASE, 컴포넌트 → PascalCase (React/Vue), private/internal 변수 → `_`로 시작 (옵션, 팀 결정) | 예: `const MAX_UPLOAD_SIZE = 10 * 1024 * 1024;` |

### 8.2 함수 선언 및 사용 규칙 (Functions)

| # | Rule | 비고 |
|---|------|------|
| 1 | **네이밍**: camelCase, 동사 + 목적어 형태 권장. (좋음: fetchUserData, calculateTotalPrice, handleSubmit / 나쁨: fn, doIt, x) | |
| 2 | **화살표 함수(⇒)** 를 기본으로 사용 (특히 콜백, 짧은 함수). `function name() {}` 은 생성자, this 바인딩 필요, 재귀 함수에만 사용 | |
| 3 | **async 함수**는 반드시 async 키워드 명시 | |
| 4 | 함수는 **한 가지 역할만** 수행. 길이 30~40줄 이상이면 리팩토링 검토 | |
| 5 | 매개변수 기본값 적극 활용: `function greet(name = 'Guest') {}` | |
| 6 | 반환은 early return 또는 단일 return 중 팀 컨벤션 따름 | |
| 7 | 익명 함수는 거의 사용하지 않는다. (map, filter 등 짧은 화살표 함수는 예외 허용) | |

### 8.3 비동기 처리 규칙 (Async/Await 중심)

| # | Rule | 비고 |
|---|------|------|
| 1 | **비동기 처리 기본 방식은 async/await**. `.then().catch()` 체인은 최대한 피한다. 콜백 스타일(callback hell)은 절대 사용하지 않는다. | |
| 2 | 모든 비동기 함수는 `async`로 선언한다. | `async function fetchUser(id) { ... }` |
| 3 | Promise를 반환하는 함수는 **await 없이 호출하지 않는다**. (fire-and-forget 금지) | |
| 4 | 병렬 비동기 처리 시 **Promise.all** 적극 활용 | `const [user, posts, comments] = await Promise.all([...])` |
| 5 | **AbortController** 적극 활용 (React 18+ concurrent mode 대비). 타임아웃·취소 가능 fetch wrapper 사용 권장 | |
| 6 | 로딩·에러 상태 관리는 **React Query, SWR, TanStack Query** 등 라이브러리 사용을 기본으로 한다. 직접 구현 시 `useState` + `useEffect`로 관리 | |

### 8.4 예외 및 오류 처리 규칙 (Error Handling)

| # | Rule | 비고 |
|---|------|------|
| 1 | **async 함수 내에서는 반드시 try-catch 사용** | |
| 2 | **커스텀 에러 클래스** 적극 활용 (도메인별 에러 구분. 예: AuthError, NetworkError) | |
| 3 | **최상위 레벨**에서 전역 에러 핸들링: React ErrorBoundary, Next.js `_error.tsx` 또는 `global-error.tsx`, Vue `app.config.errorHandler` | |
| 4 | `console.log`는 **개발 환경에서만 허용**. 프로덕션에서는 Sentry, Datadog, LogRocket 등 에러 모니터링 도구 사용 | |
| 5 | **Promise rejection은 반드시 catch 처리**. unhandledrejection 이벤트는 모니터링 도구에 연결 | |
| 6 | 예상 가능한 에러(404, 401, 403 등) → 사용자 친화적 메시지로 변환. 예상치 못한 에러(500, 네트워크 오류) → "알 수 없는 오류가 발생했습니다" + 재시도 버튼 제공 | |

### 8.5 네이밍 컨벤션 전체 요약

| 대상 | 규칙 | 예시 |
|------|------|------|
| 변수·함수 | camelCase | `fetchUserData`, `handleSubmit` |
| 상수(불변) | UPPER_SNAKE_CASE | `MAX_UPLOAD_SIZE`, `API_BASE_URL` |
| 컴포넌트 | PascalCase | `UserProfile`, `LoginForm` |
| private/internal | `_`로 시작 (옵션) | `_internalCache` |

### 8.6 금지 패턴 (Do Not)

- ❌ **var** 사용
- ❌ **.then().catch()** 체인 남용 (async/await 우선)
- ❌ **전역 변수** (window.xxx, globalThis.xxx 직접 할당)
- ❌ **fire-and-forget** (Promise 반환 함수를 await 없이 호출)
- ❌ **콜백 지옥** (callback hell)
- ❌ **dangerouslySetInnerHTML** (XSS 위험, React)

### 8.7 추천 라이브러리 & 패턴

| 용도 | 추천 |
|------|------|
| 서버 상태·캐싱 | React Query, TanStack Query, SWR |
| 폼·입력 검증 | Zod, React Hook Form, Yup |
| 에러 모니터링 | Sentry, Datadog, LogRocket |
| 상태 관리 | Zustand, Redux Toolkit, Jotai |
| HTTP 클라이언트 | Axios (인터셉터 활용), fetch + AbortController |

---

## 9. Observability & Distributed Tracing [MUST]

> 장애 대응 및 성능 분석을 위한 관찰 가능성(Observability) 필수. traceId 없는 로그 → 상관관계 불가 → 장애 대응 지연.

### 9.1 Observability 기본 원칙 [MUST]

| # | Rule | 비고 |
|---|------|------|
| 1 | 모든 서비스는 **OpenTelemetry**를 통해 Traces, Metrics, Logs 통합 수집 (3 signals unified) | |
| 2 | **로그와 트레이스 상관관계 필수**: traceId, spanId를 MDC에 자동 삽입 | |
| 3 | **샘플링 전략 명시**: 개발/스테이징 = 100%, 운영 = head-based 10~20% (critical path는 100%) | |
| 4 | **Vendor-neutral OTLP 프로토콜** 사용 (Jaeger, Zipkin, Tempo, Grafana Cloud, New Relic 등으로 export) | |

### 9.2 Spring Boot 구현 가이드 (2025~2026 베스트)

- **의존성**: `spring-boot-starter-opentelemetry` (Spring Boot 4.0+ 네이티브 지원)
- **자동 인스트루먼테이션**: Spring Web, JDBC, Kafka, Redis 등 자동 적용
- **수동 인스트루먼테이션** (비즈니스 로직 관찰 필요 시): Micrometer Observation → OpenTelemetry Span 자동 브릿지 (starter가 처리)

```java
// Micrometer Observation → OpenTelemetry Span 자동 브릿지 (starter가 처리)
ObservationRegistry registry = ObservationRegistry.create();
Observation.createNotStarted("business.operation", registry)
    .lowCardinalityKeyValue("user.id", userId)
    .observe(() -> {
        // 비즈니스 로직
    });
```

- **Collector 설정**: OpenTelemetry Collector 배포 → OTLP/gRPC 수신 → backend export
- **로그 상관관계 설정**: logback 또는 log4j2에 opentelemetry appenders 적용

### 9.3 Alerting & SLO 연계 [MUST]

| 항목 | Rule |
|------|------|
| 알림 | ERROR 이상 + 비즈니스 critical span → 즉시 PagerDuty/Slack 알림 |
| SLO 기반 alerting | 예: 99% 요청 latency < 500ms, error rate < 0.1% |
| 도구 | Prometheus + Grafana 또는 Grafana Cloud 활용 권장 |

### 9.4 금지 사항

- ❌ **traceId 없는 로그** (상관관계 불가 → 장애 대응 지연)
- ❌ **100% 샘플링 운영 환경 적용** (비용 폭증 + 성능 저하)

---

## 10. Container & Kubernetes Security [MUST / SHOULD 혼합]

> 컨테이너 및 오케스트레이션 환경(Docker, Kubernetes 등)에서 발생하는 공급망 공격, 이미지 취약점, 권한 상승, 네트워크 노출 등의 위험을 방어하기 위한 규칙.  
> OWASP Docker Security Cheat Sheet, Kubernetes Pod Security Standards (Restricted 수준), CIS Kubernetes Benchmarks v1.9+ (2025~2026 기준) 준수.

### 10.1 Container Image Security [MUST]

- **최소 베이스 이미지 사용**
  - distroless, alpine 기반 또는 Chainguard/Wolfi 같은 hardened 이미지 우선
  - ❌ full OS 이미지 (ubuntu:latest, debian:latest 등) 사용 금지 (공격면 과다)

- **이미지 태그 고정 (Pinned versions)**
  - `latest` 또는 floating tag 사용 금지
  - 예: `FROM node:20-alpine@sha256:abc123...` (digest pinning)
  - CI/CD에서 digest 검증 필수

- **자동 이미지 스캐닝** [MUST]
  - CI 빌드 단계에서 **Trivy** 또는 **Grype** 필수 실행
  - Critical/High 취약점 발견 시 빌드 실패 처리 (exit-code 1)
  - SBOM 생성 및 CycloneDX/SPDX 형식으로 아카이빙 (1.7 연계)

```yaml
# Trivy 예시 (GitHub Actions / GitLab CI)
- name: Scan container image
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: '${{ env.IMAGE_NAME }}:${{ env.IMAGE_TAG }}'
    format: 'table'
    exit-code: '1'
    ignore-unfixed: false
    severity: 'CRITICAL,HIGH'
```

- **이미지 서명 및 검증** [MUST 권장]
  - Cosign (Sigstore) 또는 Notation으로 서명
  - 배포 시 admission controller (Kyverno/OPA Gatekeeper)에서 서명 검증 강제

### 10.2 Runtime Security & Pod Hardening [MUST]

#### 10.2.1 Pod Security Standards (PSS) 적용

- 모든 네임스페이스에 **restricted** 레벨 기본 적용 (Pod Security Admission Controller 사용)
- privileged, baseline 레벨은 기술 리더 승인 및 문서화 필수
- **restricted 레벨 주요 강제 사항**
  - `runAsNonRoot: true` (루트 실행 금지)
  - `allowPrivilegeEscalation: false`
  - `runAsUser`: MustRunAsNonRoot 또는 고정 non-root UID (e.g. 10000~)
  - `capabilities`: drop ALL (필요 시 최소 추가)
  - hostPath, hostNetwork, hostPID, hostIPC: 사용 금지
  - `privileged: false`
  - `seccompProfile`: RuntimeDefault 또는 fine-grained 프로필

```yaml
# 네임스페이스 레이블 예시
metadata:
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/enforce-version: latest
```

#### 10.2.2 SecurityContext 기본 설정 [MUST]

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 10001
  runAsGroup: 10001
  fsGroup: 10001
  allowPrivilegeEscalation: false
  capabilities:
    drop: ["ALL"]
  seccompProfile:
    type: RuntimeDefault
```

#### 10.2.3 금지 사항

- ❌ `privileged: true`
- ❌ root (uid 0) 컨테이너 실행
- ❌ hostPath 마운트 (필수적 경우 readOnly + 제한 경로)
- ❌ hostNetwork/hostPID/hostIPC 사용

### 10.3 Network & Access Control [MUST]

- **NetworkPolicy 기본 적용**
  - 기본 deny-all 정책 + whitelist 방식으로 허용
  - ingress/egress 모두 명시적 정의 (default-deny)

```yaml
# 예시: deny-all ingress + 허용 규칙
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-ingress
spec:
  podSelector: {}
  policyTypes:
  - Ingress
```

- **RBAC 최소 권한**
  - ClusterRole 대신 Role 사용
  - ServiceAccount 토큰 자동 마운트 비활성화 (`automountServiceAccountToken: false`)

### 10.4 Secrets & Config Management [MUST]

- Kubernetes Secrets 대신 **Vault** 또는 **AWS Secrets Store CSI Driver** 사용 권장
- Secrets를 volume/env로 마운트 시 **read-only** + tmpfs 권장
- Long-lived SA 토큰 금지 → **IRSA (EKS)** 또는 **Workload Identity** 사용

### 10.5 Monitoring & Runtime Protection [SHOULD]

- **Falco** 또는 **Sysdig** 같은 런타임 보안 도구로 syscall, 파일 접근, 네트워크 이상 탐지
- OpenTelemetry로 컨테이너 메트릭/트레이스 수집 (9장 연계)
- Image/컨테이너 drift 탐지 (예: unexpected binary execution)

### 10.6 문서화 & 체크리스트

- 각 Helm 차트 / Deployment에 **SecurityContext** 및 **PSS 레이블** 적용 여부 명시
- 정기 **CIS Kubernetes Benchmark** 스캔 (kube-bench) 실행 및 리포트 아카이빙
- 취약 이미지/파드 발견 시 즉시 **remediation 계획** 문서화

---

## 11. 데이터 주권 및 개인정보 처리 (Data Sovereignty) [MUST]

본 섹션은 GDPR, CCPA 및 국내 개인정보보호법의 2026년 개정안을 준수하기 위한 기술적 요구사항을 정의한다.

### 11.1 데이터 최소화 및 파기 (Data Minimization & Shredding)

- **목적 기반 수집**: 서비스 제공에 반드시 필요한 **최소한의 데이터만** 수집하며, 수집 목적이 달성된 데이터는 즉시 파기한다.
- **물리적 파기**: DB 레코드 삭제 시 논리 삭제(Soft Delete)가 아닌 **물리 삭제(Hard Delete)**를 원칙으로 한다. 법령에 의해 보존이 필요한 경우 별도의 분리된 저장소(Cold Storage)로 이관하여 엄격히 통제한다.

### 11.2 사용자의 권리 보장 (Right to be Forgotten)

- **탈퇴 즉시 처리**: 사용자 탈퇴 시 해당 사용자의 PII(개인정보)는 **즉시 익명화 또는 삭제** 처리되어야 한다.
- **연관 데이터 삭제**: 주 저장소뿐만 아니라 **로그(ELK), 캐시(Redis), 백업 시스템** 내의 개인정보도 식별하여 삭제하거나 읽을 수 없는 형태로 변조한다.

### 11.3 데이터 거버넌스 및 감사 (Audit Trail)

- **DB 직접 접근 통제**: 운영자의 DB 직접 조회(DML/DQL)는 원칙적으로 **금지**하며, 긴급 상황 시 **사유 입력과 결재가 완료된 건**에 한해서만 일회성 계정을 발급한다.
- **접근 로그 보존**: 모든 개인정보 접근 기록은 **최소 2년 이상** 변조 불가능한 형태(WORM 저장소 등)로 보존한다.

### 11.4 개인정보 마스킹 및 암호화

- **화면 마스킹**: 관리자 페이지 등 UI 상에서는 이메일, 전화번호, 이름의 일부를 **반드시 마스킹** 처리한다.
- **검색 가능 암호화**: 개인정보를 검색해야 할 경우, **결정적 암호화(Deterministic Encryption)** 또는 별도의 검색용 해시 컬럼을 생성하여 평문 저장을 방지한다.

---

## 부록 A: ASVS 5.0 챕터별 대응 방안 (문제 → 대책)

ASVS 5.0(Application Security Verification Standard 5.0) 17개 챕터를 기반으로 한 **문제 → 대책** 방식의 구체적 요구사항입니다.

| 챕터 | 챕터 이름 | 문제(위험) | 대책(요구사항) | RULE 매핑 |
| ---- | --------- | ---------- | -------------- | --------- |
| **V1** | Architecture, Design and Threat Modeling | SDLC 보안 미통합, 위협 모델링 부재, 비안전 설계 | SDLC에 보안 단계 통합, 위협 모델링(STRIDE 등) 수행, Secure by Design 패턴 적용 | 2.1, 3.1 |
| **V2** | Authentication | MFA 미적용, 크리덴셜 평문 저장·전송, OAuth/JWT 불안전 구현 | MFA 적용, BCrypt/Argon2id 해싱, OAuth 2.1/OIDC/JWT 안전 구현(JWE 등) | 1.5.6, 6.1~6.3 |
| **V3** | Session Management | 세션 ID 예측 가능, 로그아웃·타임아웃 미흡, CSRF 취약 | 세션 ID 암호학적 난수(CSPRNG), 로그아웃 시 세션 무효화·타임아웃, SameSite=Strict·CSRF 토큰 | 1.5.6, 6.3 |
| **V4** | Access Control | RBAC/ABAC 미적용, IDOR, CORS 과도 허용 | RBAC/ABAC 적용, 최소 권한, 리소스 소유권 검증, CORS 허용 오리진 최소화 | 1.2 |
| **V5** | Validation, Sanitization and Encoding | 입력 미검증, XSS·인젝션 | `@Valid`·화이트리스트 검증, 출력 인코딩, DOMPurify(웹)·파라미터화 쿼리 | 1.3 |
| **V6** | Stored Cryptography | 약한 알고리즘, 키 관리 미흡, 비밀번호 취약 해싱 | AES-256-GCM, KMS/Vault 키 관리, Argon2id/PBKDF2 비밀번호 해싱 | 1.5.2 |
| **V7** | Error Handling and Logging | 스택 트레이스 노출, 로그에 민감정보, 모니터링 부재 | 스택 트레이스 사용자 반환 금지, 구조화 로깅, 민감정보 마스킹, 모니터링·알림 설정 | 1.4, 2.2 |
| **V8** | Data Protection | 과다 저장, 마스킹 미적용, 백업 암호화 미흡 | 데이터 분류·최소 저장, PII 마스킹, 백업 암호화 | 1.1 |
| **V9** | Communications | TLS 미적용·약함, HSTS 미설정, 인증서 검증 생략 | TLS 1.3 강제, HSTS 헤더, Certificate Pinning(모바일), HTTP/2·3 | 1.5.1 |
| **V10** | Malicious Code / Code Tampering | 무결성 미검증, 서명 부재, 런타임 변조 | 아티팩트 서명·검증, RASP/런타임 보호 고려 | 1.5.3, 1.7 |
| **V11** | Business Logic | 레이스 컨디션, 무제한 쿠폰·포인트 악용 | 동시성 제어(분산 락), 비즈니스 규칙 검증, 서비스 계층 검증 | 2.1, 2.3 |
| **V12** | Files and Resources | 파일 업로드 검증 누락, 경로 traversal, 안전 저장 미흡 | 파일 타입·크기·콘텐츠 검증, 경로 traversal 방지, 외부 저장 경로 분리 | 1.3 |
| **V13** | API Security | REST/GraphQL 인증·검증 부재, Rate Limiting 없음 | API 인증·권한 검사, Rate Limiting, API 키·토큰 안전 관리 | 2.1, 1.5.4 |
| **V14** | Configuration | Actuator 노출, 불필요 기능 활성화, 환경 혼재 | Actuator 제한·인증, 불필요 기능 비활성화, 환경별 설정 분리 | 1.6, 5.1 |
| **V15** | WebSockets / Real-time | WebSocket 인증·권한 미흡, 메시지 미검증, DoS | WebSocket 핸드셰이크 인증·권한, 메시지 검증, DoS 방어(제한) | 1.2, 1.5.4 |
| **V16** | Mobile | 루트/탈옥 탐지 미흡, Secure Storage 미사용, Cert Pinning 없음 | 루트·탈옥 탐지(필요 시), flutter_secure_storage/Keychain 사용, Certificate Pinning | 7.3 |
| **V17** | Software Supply Chain & Dependencies | 의존성 CVE, SBOM 부재, 공급망 공격 | 의존성 스캔(dependency-check, Dependabot), SBOM 생성, 서명 검증 | 1.7 |

### ASVS 5.0 챕터별 상세 대책 요약

#### V1 Architecture, Design and Threat Modeling

- **문제**: 설계 단계 보안 고려 부재
- **대책**: 신규 기능/API 설계 시 위협 모델링, STRIDE 분석, 계층별 보안 요구사항 정의

#### V2 Authentication

- **문제**: 인증 우회, 크리덴셜 탈취
- **대책**: MFA, 비밀번호 Argon2id/BCrypt, JWT 15분 이하, OAuth 2.1/OIDC 표준 준수

#### V3 Session Management

- **문제**: 세션 고정·탈취, CSRF
- **대책**: 세션 ID CSPRNG, 로그아웃 시 완전 무효화, SameSite=Strict, CSRF 토큰

#### V4 Access Control

- **문제**: IDOR, 권한 상승, CORS 오설정
- **대책**: deny-by-default, 리소스별 소유권 검증, CORS 화이트리스트

#### V5 Validation, Sanitization and Encoding

- **문제**: XSS, SQL/NoSQL 인젝션
- **대책**: @Valid·화이트리스트, 파라미터 바인딩, 출력 인코딩, DOMPurify(프론트)

#### V6 Stored Cryptography

- **문제**: 평문 저장, 약한 해싱
- **대책**: AES-256-GCM, KMS/Vault, Argon2id/PBKDF2

#### V7 Error Handling and Logging

- **문제**: 스택 트레이스 노출, 로그 유출, 모니터링 부재
- **대책**: GlobalExceptionHandler, 구조화 로깅, 민감정보 마스킹, 침해 탐지 알림

#### V8 Data Protection

- **문제**: 과다 수집·저장, PII 노출
- **대책**: 데이터 분류, 최소 수집, 마스킹, 백업 암호화

#### V9 Communications

- **문제**: 평문 전송, TLS 미적용
- **대책**: TLS 1.3, HSTS, 인증서 검증, 모바일 Cert Pinning

#### V10 Malicious Code / Code Tampering

- **문제**: 빌드/배포 변조
- **대책**: 아티팩트 서명·검증, Deserialization 화이트리스트

#### V11 Business Logic

- **문제**: 레이스 컨디션, 쿠폰·포인트 무제한 악용
- **대책**: Redisson 분산 락, Service 계층 비즈니스 검증

#### V12 Files and Resources

- **문제**: 악성 파일 업로드, 경로 traversal
- **대책**: 타입·크기·콘텐츠 검증, 저장 경로 분리, 파일명 정규화

#### V13 API Security

- **문제**: API 인증/권한 누락, Rate Limiting 부재
- **대책**: 모든 API 인증·권한 검사, Rate Limiting, 토큰 안전 관리

#### V14 Configuration

- **문제**: Actuator·디버그 노출, 환경 혼재
- **대책**: Actuator 제한·인증, 기본값 변경, local/dev/prod 분리

#### V15 WebSockets / Real-time

- **문제**: WebSocket 인증 미흡, 메시지 미검증, DoS
- **대책**: 핸드셰이크 인증·권한, 메시지 검증·Rate Limiting

#### V16 Mobile

- **문제**: Secure Storage 미사용, 루트/탈옥 환경
- **대책**: flutter_secure_storage, Certificate Pinning, 루트 탐지(필요 시)

#### V17 Software Supply Chain & Dependencies

- **문제**: 의존성 CVE, SBOM 부재, 공급망 공격
- **대책**: dependency-check, Dependabot, SBOM 생성, 아티팩트 서명

---

## 부록 B: OWASP Top 10 2025 매핑표

| 순위 | 코드 | 영문 명칭 | 2021 대비 | 주요 위험 및 대응 |
| ---- | ---- | --------- | --------- | ----------------- |
| 1 | A01:2025 | Broken Access Control | #1 유지 (SSRF 통합) | IDOR, 강제 브라우징, CORS 오설정, SSRF → 1.2 |
| 2 | A02:2025 | Security Misconfiguration | #5 → #2 급상승 | 기본값 노출, 포트·버킷 공개, 에러 정보 유출 → 1.6 |
| 3 | A03:2025 | Software Supply Chain Failures | 신규 | 의존성·빌드·배포 체인 CVE 모니터링 → 1.7 |
| 4 | A04:2025 | Cryptographic Failures | #2 → #4 | 약한 알고리즘, 평문 전송, 키 관리 미흡 → 1.5.2 |
| 5 | A05:2025 | Injection | #3 → #5 | SQL/NoSQL/OS/LDAP 인젝션 → 1.3 |
| 6 | A06:2025 | Insecure Design | #4 → #6 | 위협 모델링 부족, 안전 설계 패턴 → 2.1 |
| 7 | A07:2025 | Authentication Failures | 순위 유지 | 인증 우회, 스터핑, 세션 고정, 브루트포스 → 1.5.6 |
| 8 | A08:2025 | Software and Data Integrity Failures | 순위 유지 | CI/CD, deserialization 무결성 → 1.5.3 |
| 9 | A09:2025 | Security Logging and Monitoring Failures | 순위 유지 | 로그 누락·조작, 모니터링 미흡 → 1.4 |
| 10 | A10:2025 | Mishandling of Exceptional Conditions | 신규 | 스택 트레이스 노출, DoS, 상태 불일치 → 2.2 |

---

## 부록 C: 규칙 준수 체크리스트

| 카테고리 | 체크 항목 | 필수 |
| ---------- | ----------- | ------ |
| 보안 | 비밀정보 외부 주입 방식 사용 | ✅ |
| 보안 | 인증 실패 시 401 반환 | ✅ |
| 보안 | 권한 부족 시 403 반환 | ✅ |
| 보안 | 입력값 검증 적용 | ✅ |
| 보안 | 로그에 민감정보 미포함 | ✅ |
| 보안 (Secrets) | 자동 로테이션 필수, 장기 정적 secrets 180일 초과 금지 (RULE 1.1.3~1.1.4) | ✅ |
| 보안 (Secrets) | Grace Period·지수 백오프·Rotation Health Check·긴급 롤백 절차 (RULE 1.1.3.1) | ✅ |
| 데이터 주권 (11.x) | 물리 삭제 원칙, 탈퇴 즉시 익명화·삭제, 연관 데이터(로그·캐시·백업) 삭제 | ✅ |
| 데이터 주권 (11.x) | DB 직접 접근 통제·결재, 접근 로그 2년 보존, PII 마스킹·검색 가능 암호화 | ✅ |
| 보안 (Rate Limiting) | 공개 API Rate Limiting 적용, 429 + Retry-After 반환 (RULE 1.9) | ✅ |
| 보안 (로깅) | SLF4J 사용, 파라미터화 로깅 `{}`, 로그 레벨 준수 (RULE 1.4.3) | ✅ |
| Observability (9.x) | OpenTelemetry, traceId/spanId MDC 삽입, 100% 샘플링 운영 금지 | ✅ |
| Container/K8s (10.x) | Trivy/Grype 이미지 스캔, PSS restricted, SecurityContext, NetworkPolicy deny-all | ✅ |
| 기능 | HTTP Method 의미 준수 | ✅ |
| 기능 | 공통 예외 체계 사용 | ✅ |
| 기능 | 트랜잭션 경계 Service 계층 | ✅ |
| 기술 | 계층 간 단방향 의존성 | ✅ |
| 기술 | N+1 문제 대응 | ✅ |
| 기술 | 엔티티 직접 반환 금지 | ✅ |
| 기술 | 외부 호출 Timeout 설정 | ✅ |
| 기술 (AOP) | 횡단 관심사 전용, 비즈니스 로직·상태 변경 금지 | ✅ |
| 기술 (AOP) | Pointcut 명시적·Annotation 기반, @Transactional Service만 | ✅ |
| 기술 (AOP) | AOP 예외 기록 후 재throw, 다중 AOP 시 @Order 명시 | ✅ |
| 기술 (AOP) | AOP 추가 시 문서화(Pointcut·목적·영향도) | ✅ |
| 품질 | 핵심 로직 테스트 존재 | ✅ |
| 품질 (테스트) | Given-When-Then 패턴, // given / when / then 주석 필수 (RULE 4.2.2) | ✅ |
| 품질 (테스트) | AssertJ 사용, BDDMockito 권장, @DisplayName 필수 | ✅ |
| 품질 (테스트) | 테스트 메서드 이름 BDD 스타일 (메서드명_상황_should결과) | ✅ |
| 품질 | API 문서화 (Swagger) | ✅ |
| 품질 (주석) | public API Javadoc 필수, 한글 주석 기본 (RULE 4.4) | ✅ |
| 운영 | 환경별 설정 분리 | ✅ |
| 운영 | Fallback 전략 정의 | ✅ |
| Spring Boot | DB 등 민감정보 환경변수 주입 | ✅ |
| Spring Boot | GlobalExceptionHandler로 예외 통일 반환 | ✅ |
| React | dangerouslySetInnerHTML 금지, 토큰 메모리/HttpOnly | ✅ |
| React | 공통 Axios 인스턴스 + 토큰 인터셉터 | ✅ |
| JavaScript (8.x) | var 금지, const > let, 전역 변수 금지 | ✅ |
| JavaScript (8.x) | async/await 기본, .then 체인·콜백 지옥 금지 | ✅ |
| JavaScript (8.x) | async 함수 내 try-catch, 전역 에러 핸들링(ErrorBoundary 등) | ✅ |
| Flutter | SharedPreferences 대신 flutter_secure_storage | ✅ |
| Flutter | 비동기 try-catch + 사용자 친화적 에러 메시지 | ✅ |
| OWASP A01 | IDOR·SSRF·CORS 검증 | ✅ |
| OWASP A02 | 보안 설정(기본값·에러 정보 미노출) | ✅ |
| OWASP A03 | 의존성 CVE 모니터링 | ✅ |
| OWASP A10 | 스택 트레이스 사용자 반환 금지 | ✅ |
| ASVS V1 | 위협 모델링·Secure by Design | ✅ |
| ASVS V4 | IDOR·CORS 검증 | ✅ |
| ASVS V7 | 구조화 로깅·민감정보 마스킹 | ✅ |
| ASVS V13 | API Rate Limiting·토큰 관리 | ✅ |
| ASVS V14 | Actuator 제한·환경 분리 | ✅ |
| ASVS V17 | 의존성 스캔·SBOM | ✅ |
| JWT | 모든 JWT 서명·클레임 검증 (alg allow-list, iss/aud) | ✅ |
| JWT | jti 포함, Revocation(블랙리스트) 지원 | ✅ |
| JWT | 웹: 쿠키(HttpOnly/Secure/SameSite), localStorage 금지 | ✅ |
| RSA/키 | 개인키 HSM/Vault/KMS, 단일 목적 원칙 | ✅ |
| OAuth/OIDC | Authorization Code + PKCE, Implicit/Password Grant 금지 | ✅ |
| OAuth/OIDC | Redirect URI allow-list, State/Nonce 필수 | ✅ |

---

> **마지막 업데이트**: 2026-02-05
> **버전**: 1.0.5 (1.1.3.1 Secrets Rotation Grace Period, 섹션 11 데이터 주권·개인정보 처리, 운영 원칙 추가)
