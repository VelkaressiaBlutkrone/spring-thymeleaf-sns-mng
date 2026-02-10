# 1. 보안(Security) RULE — 절대 예외 없음 [MUST]

> 원본: [RULE.md](../RULE.md) 1장. 일상 참조 시 본 파일 또는 부록 C 체크리스트 활용.

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

| Secret 종류           | 로테이션 주기                                  | 구현 방법 추천                    | 비고                      |
| --------------------- | ---------------------------------------------- | --------------------------------- | ------------------------- |
| DB 비밀번호           | 30~90일                                        | AWS Secrets Manager + Lambda 자동 | RDS 연동                  |
| API Key / 토큰 서명키 | 90일 이내                                      | HashiCorp Vault dynamic secrets   | JWKS 로테이션 연계        |
| JWT signing key pair  | 90~180일                                       | Vault 또는 KMS + 자동 재배포      | 무중단 로테이션 계획 필수 |
| Encryption key (KMS)  | AWS-managed: 자동 / Customer-managed: 90~365일 | 자동 활성화                       | 최소 1회 로테이션 증빙    |

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

| 레벨  | 심각도    | 사용 시점                                       | local/dev | prod  |
| ----- | --------- | ----------------------------------------------- | --------- | ----- |
| TRACE | 최저      | 매우 상세 내부 추적 (거의 사용 금지)            | OFF       | OFF   |
| DEBUG | 낮음      | 개발/테스트용 상세 정보 (SQL 바인딩, 객체 상태) | DEBUG     | OFF   |
| INFO  | 보통      | 서버 기동/종료, 주요 비즈니스 이벤트 성공       | INFO      | INFO  |
| WARN  | 높음      | 비정상이지만 계속 진행 가능 (재시도, 대체 경로) | INFO      | WARN  |
| ERROR | 매우 높음 | 요청/기능 실패, 시스템 전체는 동작              | INFO      | ERROR |

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

| 항목         | local/dev        | staging                  | prod                              |
| ------------ | ---------------- | ------------------------ | --------------------------------- |
| 출력         | 콘솔 + 파일      | 콘솔 + 파일 + 중앙 수집  | 파일 + 중앙 수집                  |
| 경로         | `./logs/`        | `/app/logs/`             | `/var/log/{service}/`             |
| 파일명       | `app.log`        | `app-%d{yyyy-MM-dd}.log` | `app-%d{yyyy-MM-dd-HH}.%i.log.gz` |
| 롤링         | 10MB 또는 일단위 | 일단위 + 50MB            | 시간단위(1h) + 100MB              |
| 압축         | 없음             | gzip                     | gzip                              |
| 보관 기간    | 7일              | 30일                     | 90일(Hot) + 1년(Cold)             |
| 최대 파일 수 | 10개             | 30개                     | 720개(1h×30일)                    |

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

- **기본 원칙**: 모든 API는 **HTTPS(TLS 1.3)** 위에서 동작한다. 전송 구간 기밀성은 **TLS로 충분**하며, 대부분의 서비스에서는 애플리케이션 레벨 이중 암호화 없이 운영한다.
- **애플리케이션 레벨 암호화**는 **특별히 높은 기밀성이 요구되는 경우**에만 적용한다(예: 클라이언트 저장 데이터 암호화, 특정 필드별 암호화 요구).
  - 적용 시: 하이브리드 암호화(RSA+AES-GCM) 등 명시된 방식을 사용할 수 있음. 서버 RSA 공개키 제공, 클라이언트 AES 키 생성·RSA 래핑, 전송 형식 `{encryptedKey, iv, encryptedData}` 등.
- 고빈도 트래픽·성능 부담이 큰 구간은 **내부망 보안이 보장된 경우**에 한해 기술 리더 승인 및 위험·대안 문서화 후 암호화 범위 조정을 검토할 수 있다.

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
- **Prompt Injection 방어**: 사용자 입력을 그대로 프롬프트에 삽입하지 않고, 역할·경계 구분(시스템/사용자 메시지 분리), 입력 길이·패턴 제한, 위험 패턴 필터링 적용

#### 1.8.2 AI 출력 검증 및 Hallucination 대응

- **AI 모델 출력은 신뢰하지 않는다**: 구조화된 출력(JSON 등)은 스키마 검증, 비즈니스 규칙 재검증 필수
- Hallucination·환각 대응: 중요한 결정·사실 반영 시 사람 검토 또는 소스 검증 절차 권장

#### 1.8.3 AI API 호출 감사

- **AI API 호출에 대한 감사 로그** 기록(요청/응답 요약, 토큰 사용량, 호출자·타임스탬프). 민감 입력/출력은 로그에 포함하지 않는다.

#### 1.8.4 AI 생성 코드 도입

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

> **맥락**: 아래 수치는 **프로젝트 초기 기본값**이며, 서비스 규모(B2C 대규모 vs 내부 관리 시스템 등)에 따라 크게 다를 수 있다. **트래픽 분석 후 조정**하는 것을 권장한다.

| 대상            | 제한 예시                  | 구현 라이브러리          | 비고                   |
| --------------- | -------------------------- | ------------------------ | ---------------------- |
| 로그인/인증     | 5~10 req / 1분 (IP+계정)   | Bucket4j + Redis         | 계정 잠금 연계         |
| 토큰 발급/갱신  | 20 req / 5분               | Bucket4j                 | Refresh 토큰 남용 방지 |
| 일반 API (읽기) | 300~1000 req / 분 (userId) | Resilience4j RateLimiter | 글로벌 vs 사용자별     |
| 민감 API (쓰기) | 50 req / 분                | Bucket4j                 | 데이터 변경 작업       |
| 비인증 API      | 100 req / 분 (IP 기준)     | Spring Cloud Gateway     | 봇/크롤러 방어         |

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
