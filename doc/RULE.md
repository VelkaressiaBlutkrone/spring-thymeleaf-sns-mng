# 프로젝트 개발 규칙 (RULE Book)

> **이 규칙은 프로젝트의 도메인, 규모, 일정과 무관하게 반드시 준수한다.**
> **예외가 필요한 경우 반드시 기술 리더의 승인과 문서화를 거친다.**
> **규칙 위반은 코드 리뷰에서 반려 사유가 된다.**

---

## 1. 보안(Security) RULE — 절대 예외 없음

### 1.1 비밀정보 관리

#### 1.1.1 반드시 지켜야 함

- 비밀번호, 토큰, API Key, 인증서 **소스코드 하드코딩 금지**
- `.env`, Secret Manager, Vault, KMS 등 **외부 주입 방식만 허용**
- Git 저장소에 비밀정보 커밋 시 **즉시 폐기 + 키 재발급**

#### 1.1.2 금지

- ❌ `application.yml`에 평문 비밀번호
- ❌ 테스트용이라는 이유로 키를 코드에 남기는 행위

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

---

## 2. 기능(Function) RULE — 일관성과 예측 가능성

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

### 2.3 트랜잭션 규칙

#### 2.3.1 트랜잭션 경계

- 트랜잭션 경계는 **Service 계층**
- 읽기 전용 조회는 `readOnly = true`
- 외부 API 호출을 트랜잭션 내부에 두지 않는다

### 2.4 상태 관리

#### 2.4.1 상태 관리 원칙

- 서버는 가능한 한 **Stateless**
- 세션 사용 시:
  - 세션 스토리지 명시
  - 확장성 고려(Sticky session 전제 금지)

---

## 3. 기술(Technical) 제한 RULE — 프로젝트 생존성

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

---

## 4. 품질(Quality) RULE — 유지보수 가능성

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

### 4.3 문서 규칙

#### 4.3.1 문서화 기준

- 공개 API → **Swagger/OpenAPI 필수**
- 설정 값 → README 또는 config 문서화
- ❌ "코드 보면 안다" 금지

---

## 5. 운영(Operation) RULE — 사고 대응

### 5.1 설정 분리 (OWASP A02:2025 연계)

#### 5.1.1 분리 원칙

- 환경별 설정 분리(`local` / `dev` / `prod`)
- 운영 환경에서 개발용 설정 금지

### 5.2 장애 대비

#### 5.2.1 장애 대응

- 모든 외부 연동 실패 시 **Fallback 전략 정의**
- 장애 발생 시 **로그만 보고 원인 추적 가능**해야 함

---

## 6. 인증/토큰 관리 규칙

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
| 기능 | HTTP Method 의미 준수 | ✅ |
| 기능 | 공통 예외 체계 사용 | ✅ |
| 기능 | 트랜잭션 경계 Service 계층 | ✅ |
| 기술 | 계층 간 단방향 의존성 | ✅ |
| 기술 | N+1 문제 대응 | ✅ |
| 기술 | 엔티티 직접 반환 금지 | ✅ |
| 기술 | 외부 호출 Timeout 설정 | ✅ |
| 품질 | 핵심 로직 테스트 존재 | ✅ |
| 품질 | API 문서화 (Swagger) | ✅ |
| 운영 | 환경별 설정 분리 | ✅ |
| 운영 | Fallback 전략 정의 | ✅ |
| Spring Boot | DB 등 민감정보 환경변수 주입 | ✅ |
| Spring Boot | GlobalExceptionHandler로 예외 통일 반환 | ✅ |
| React | dangerouslySetInnerHTML 금지, 토큰 메모리/HttpOnly | ✅ |
| React | 공통 Axios 인스턴스 + 토큰 인터셉터 | ✅ |
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

> **마지막 업데이트**: 2026-02-04
> **버전**: 1.0.0
