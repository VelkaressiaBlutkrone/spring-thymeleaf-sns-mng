# 지도 기반 SNS 웹/모바일 통합 플랫폼 — TASK (단계별 작업)

- **기준 문서**: `doc/PRD.md`, `doc/RULE.md`
- **총 기간**: 102일 내외
- **총 Step**: 21단계 (Step 5.1 포함)

---

## 주석 규칙 (RULE 4.4)

- 모든 Step 산출물은 **RULE 4.4(주석 규칙)** 준수
- public API → Javadoc 필수 (한 줄 요약 → 상세 → @param/@return/@throws)
- 주석은 **한글** 기본, Why(이유) 설명, 코드와 동기화

---

## 로깅 규칙 (RULE 1.4.3)

- 로깅이 필요한 Step은 **RULE 1.4.3(로깅 기술 규칙)** 준수
- SLF4J `@Slf4j` 또는 `LoggerFactory` 사용, `{}` 파라미터화 로깅, 문자열 concat 금지
- 로그 레벨: ERROR(예외·실패), WARN(비정상·재시도), INFO(주요 이벤트), DEBUG(개발용)
- 민감정보(PII, 토큰, 비밀번호) 로그 출력 금지, `System.out`/`System.err` 금지

---

## 테스트 규칙 (RULE 4.2.2, 4.2.1.1, 1.2.4)

- 테스트 작성 Step은 **RULE 4.2.2(테스트 코드 작성 규칙)** 준수
- **Given-When-Then 패턴** 필수, `// given`, `// when`, `// then` 주석 3줄 필수
- **AssertJ** 사용(JUnit Assertions 대신), **BDDMockito** 권장(given/willReturn)
- **Mockito** (Java) / **MockK** (Kotlin) 목 객체 생성
- 테스트 메서드 이름: BDD 스타일 (`[메서드명]_[상황]_should[결과]` 또는 `should[결과]_when[상황]`)
- `@DisplayName` 필수(클래스·메서드 모두), 예외 테스트는 `assertThatThrownBy()` 사용
- 단위 테스트: `@ExtendWith(MockitoExtension.class)` · Controller: `@WebMvcTest`+`@MockBean` · Repository: `@DataJpaTest` · 통합: `@SpringBootTest`(최소화)
- **테스트 결정성 (4.2.1.1)**: `System.currentTimeMillis()`, `LocalDateTime.now()` 직접 사용 금지 → `Clock`, `TimeProvider` 추상화. 랜덤값은 고정 시드 또는 Stub
- **인증·인가 테스트 (1.2.4)**: 보호 API는 401(미인증)·403(권한 부족) 테스트 최소 1개 이상 필수

---

## Step 템플릿 설명 (RULE 4.3.2)

> 신규 Step 작성·추가 시 **RULE 4.3.2(Task 문서 작성 구조)** 를 반드시 준수한다.

| 필드               | 설명                                     |
| ------------------ | ---------------------------------------- |
| **Step Name**      | 단계 이름                                |
| **Step Goal**      | 단계 완료 시 달성할 목표(한 문장)        |
| **Input**          | 이 단계에 필요한 입력(문서·코드·환경 등) |
| **Scope**          | 포함/제외 범위로 단계 경계 명확화        |
| **Instructions**   | 수행할 작업 목록                         |
| **Output Format**  | 산출물 형태·위치·형식                    |
| **Constraints**    | 반드시 지켜야 할 제약(RULE·기술 등)      |
| **Done When**      | 아래 조건 충족 시 단계 완료로 간주       |
| **Duration**       | 예상 소요 일수                           |
| **RULE Reference** | 참조할 RULE.md 섹션                      |

---

## 일정 요약

| Phase      | Step 범위    | 기간(일) | 비고                                                     |
| ---------- | ------------ | -------- | -------------------------------------------------------- |
| 설계·기반  | Step 1 ~ 5.1 | 27       | 아키텍처, ERD, 공통 인프라, 인증 설계, DB·Redis Fallback |
| 회원·보안  | Step 6 ~ 7   | 11       | 회원 도메인, Spring Security, 역할                       |
| 게시판     | Step 8 ~ 9   | 11       | Post, ImagePost, 파일 업로드                             |
| 지도·Pin   | Step 10 ~ 13 | 22       | Pin/Location, 지도 추상화, 웹 UI, 연동                   |
| 마이·About | Step 14      | 4        | 마이페이지, About                                        |
| 관리자     | Step 15 ~ 17 | 14       | 회원/게시물 관리, 통계                                   |
| 마무리     | Step 18 ~ 20 | 13       | 보안 점검, 테스트·문서, 배포                             |
| **합계**   | **20+1**     | **102**  | Step 5.1 포함                                            |

---

## Step 1 — 프로젝트 셋업·아키텍처·ERD

**Step Name:** 프로젝트 셋업·아키텍처·ERD

**Step Goal:** 저장소·빌드·전체 시스템 구조·ERD를 확정하여 이후 단계의 공통 기준을 마련한다.

**Input:**

- PRD 2(기술 스택), 4(아키텍처·데이터 설계)
- 기존 Gradle/Spring Boot 프로젝트(있는 경우)

**Scope:**

- 포함: Gradle·모듈 셋업, 아키텍처 다이어그램, ERD(User, Post, ImagePost, Pin, Location, Post↔Pin), 환경별 `application-*.yml` 골격(local/dev/prod)
- 제외: 실제 비즈니스 코드 구현, 외부 서비스(Redis/MySQL) 상세 튜닝

**Instructions:**

- Gradle·Java 21·Spring Boot 4.0.2 기반 프로젝트/모듈 구조 확인·정리
- 전체 시스템 아키텍처 다이어그램 작성(웹/모바일·Backend·DB·Redis·지도 API)
- ERD 설계: User, Post, ImagePost, Pin, Location, Post↔Pin 연관 관계
- `application-local.yml`, `application-dev.yml`, `application-prod.yml` 골격 작성(비밀정보는 플레이스홀더 또는 환경 변수 참조만)
- **로깅 (RULE 1.4.3)**: 환경별 `logging.level`, `logging.file`, `logback.rollingpolicy` 골격 포함

**Output Format:**

- 아키텍처 다이어그램: `doc/` 또는 `docs/` 내 이미지/마크다운
- ERD: `doc/` 내 다이어그램 또는 테이블 정의
- 설정 파일: `src/main/resources/application-*.yml`

**Constraints:**

- RULE 1.1(비밀정보 환경 변수 주입), 5.1(환경별 설정 분리), 3.1(계층 분리) 준수
- `application.yml`에 평문 비밀번호·API Key 금지

**Done When:**

- 아키텍처 다이어그램·ERD가 문서로 존재하고, 환경별 설정 골격이 적용된 상태에서 빌드가 성공한다.

**Duration:** 5일

**RULE Reference:** 1.1, 5.1, 3.1, 1.4.3(로깅)

---

## Step 2 — 공통 인프라 (예외·Validation·보안 설정·AOP)

**Step Name:** 공통 인프라 (예외·Validation·보안 설정·AOP)

**Step Goal:** GlobalExceptionHandler·ErrorCode·Validation·보안 헤더를 도입하고, 횡단 관심사용 AOP(로깅·감사·예외 보조)를 RULE 3.5에 맞게 적용하여 예외·에러·보안·관찰을 일관되게 한다.

**Input:**

- Step 1 완료(패키지 구조·ERD)
- RULE 2.2(예외 처리), 1.6(보안 설정), **3.5(AOP 개발 규칙)**

**Scope:**

- 포함: 공통 예외 체계(BusinessException, ErrorCode, ErrorResponse), @Valid 기반 검증, HSTS/X-Content-Type-Options/CSP 등 보안 헤더, Actuator 노출 제한, **횡단 관심사용 AOP(Access Log·Audit Log·예외 처리 보조)** 설계·구현
- 제외: 도메인별 상세 비즈니스 검증, OAuth2·JWT 구현, **비즈니스 로직·상태 변경을 포함한 AOP**

**Instructions:**

- `BusinessException`, `ErrorCode` enum, `ErrorResponse` DTO 정의
- `@RestControllerAdvice` 기반 `GlobalExceptionHandler` 구현(스택 트레이스 사용자 반환 금지)
- Controller 요청 DTO에 `@Valid` 및 검증 어노테이션 적용
- 보안 헤더 필터 또는 설정(HSTS, X-Content-Type-Options, CSP 등) 적용
- Actuator 엔드포인트 제한·인증(필요 시)
- **AOP 적용 (RULE 3.5 준수)**
  - Access Log / Audit Log용 Aspect: Pointcut은 **명시적·좁게** 또는 **Annotation 기반** (예: `@annotation(LogAccess)`), 패키지 전체 포인트컷 금지
  - 예외 처리 보조: 예외 catch 시 **로깅만 하고 반드시 재throw** (예외 판단·변환·return null 금지)
  - 다중 Aspect 사용 시 **@Order 명시** (예: Security → Transaction → Logging)
  - AOP 내부에서 **상태 변경·Entity/Request 수정·비즈니스 판단 금지**
  - **AOP 추가 시 문서화**: 적용 대상(Pointcut), 목적, 실행 시점, 예외 처리 정책, 제거 시 영향도 (README 또는 doc/)
- **로깅 규칙 (RULE 1.4.3)** 적용
  - SLF4J `@Slf4j` 또는 `LoggerFactory` 사용
  - 파라미터화 로깅 `{}` placeholder 사용, 문자열 concat 금지
  - 로그 레벨: ERROR(예외), WARN(비정상), INFO(주요 이벤트), DEBUG(개발용)

**Output Format:**

- 코드: `exception/`, `config/`, `aop/`(또는 `aspect/`) 등 계층에 맞는 패키지
- ErrorCode·ErrorResponse: API 응답 스펙과 일치하는 JSON 구조
- **AOP 문서**: Pointcut·목적·Order·예외 정책·제거 시 영향도

**Constraints:**

- RULE 2.2(공통 예외, 스택 트레이스 미노출), A02 Security Misconfiguration(1.6) 준수
- **RULE 3.5(AOP)**: 횡단 관심사 전용, Pointcut 명시적·Annotation 우선, 예외 기록 후 재throw, 상태 변경 금지, @Order 명시, 문서화 필수
- 에러 발생 시 200 OK 반환 금지

**Done When:**

- 미정의 예외 발생 시 GlobalExceptionHandler를 통해 일관된 ErrorResponse가 반환되고, 보안 헤더가 적용되어 있으며, Actuator가 제한되어 있다.
- **AOP가 적용된 경우**: 제거해도 시스템이 정상 동작하며, Pointcut·Order·문서가 존재한다.

**Duration:** 5일

**RULE Reference:** 2.2, 1.6, **3.5**, **1.4.3(로깅)**, 4.4(주석)

---

## Step 3 — 패키지 구조·엔티티·Repository

**Step Name:** 패키지 구조·엔티티·Repository

**Step Goal:** Controller/Service/Domain/Repository 패키지와 핵심 엔티티·Repository를 구현하여 데이터 계층과 도메인 모델을 확정한다.

**Input:**

- Step 1 ERD, PRD 4.1(계층·데이터 설계)

**Scope:**

- 포함: 계층별 패키지 생성, User/Post/ImagePost/Pin 엔티티, Location Value Object, JPA Repository 인터페이스, DTO 골격
- 제외: 비즈니스 로직 구현, REST API 핸들러 구현, 지도·파일 업로드 상세

**Instructions:**

- `controller/`, `service/`, `domain/`(또는 `entity/`), `repository/`, `dto/` 패키지 구조 정립
- User, Post, ImagePost, Pin 엔티티 및 Location(임베디드 또는 VO) 구현
- JPA Repository 인터페이스 정의
- 목록/상세 등에 사용할 DTO 클래스 골격 작성(엔티티 직접 반환 금지)

**Output Format:**

- 패키지 구조: `doc/` 또는 코드 트리로 문서화
- 엔티티: `@Entity`, `@Table` 등 JPA 규칙 준수
- Repository: `JpaRepository` 또는 동일 규약 상속

**Constraints:**

- RULE 3.1(Controller→Service→Repository 단방향), 3.3(엔티티 API 직접 반환 금지, N+1 대응 설계)
- Domain이 Infra(Spring/JPA)에 과도히 종속되지 않도록 설계
- **RULE 3.5.5**: `@Transactional`은 이 단계에서 Repository·Entity에 선언하지 않음 — Service 계층에서만 사용

**Done When:**

- 모든 핵심 엔티티·Repository가 존재하고, 빌드 및 (선택) 스키마 생성/마이그레이션이 성공한다.

**Duration:** 5일

**RULE Reference:** 3.1, 3.3, **3.5.5**, 4.4(주석), 1.4.3(로깅)

---

## Step 4 — REST API 명세 확정·인증 설계

**Step Name:** REST API 명세 확정·인증 설계

**Step Goal:** 회원·게시글·Pin·관리자 등 REST API 목록과 **JWT 기반 인증** 방식을 확정하고, Swagger 골격을 갖춘다.

**Input:**

- PRD 3(핵심 기능), 4.1(계층·API), Step 1 ERD

**Scope:**

- 포함: API 목록·Method·Request/Response 정의, **JWT(Access+Refresh Token)** 인증 설계, Swagger/OpenAPI 설정
- 제외: 실제 API 구현 코드, OAuth2 구현

**Instructions:**

- 회원(가입/로그인/조회), 게시글(목록/상세/작성/수정/삭제), Pin(CRUD), 관리자(회원·게시물·통계) 등 API 목록 작성
- 각 API별 HTTP Method, URL, Request/Response 본문·쿼리 파라미터 정의
- **JWT 기반 인증 흐름 정리 (RULE 6.1~6.5 준수)**: Access Token(15분 이하), Refresh Token(Redis), jti·Revocation, iss/aud 검증
- OAuth2 확장 시 필요한 확장 포인트(엔드포인트·역할) 정리
- SpringDoc/Swagger 설정으로 API 문서 골격 노출
- **로깅 (RULE 1.4.3)**: Swagger/Actuator 접근 등 공개 엔드포인트에 무의미한 반복 로그 금지

**Output Format:**

- REST API 명세서: `doc/` 내 마크다운 또는 스프레드시트
- 인증 설계: `doc/` 내 문서
- Swagger: `/swagger-ui.html` 등으로 접근 가능

**Constraints:**

- RULE 2.1(HTTP Method 준수, API당 명확한 책임), 4.3(공개 API Swagger 필수), **6.1~6.5(JWT·토큰 관리)**

**Done When:**

- REST API 명세서와 인증 설계 문서가 존재하고, Swagger UI로 (빈 또는 스텁 포함) API 목록이 노출된다.

**Duration:** 5일

**RULE Reference:** 2.1, **2.1.3(내부/Admin API 분리)**, 4.3, 1.4.3(로깅), **6.1~6.5(JWT)**

---

## Step 5 — Redis 연동·JWT·캐시 설정

**Step Name:** Redis 연동·JWT·캐시 설정

**Step Goal:** Redis를 연결하고 **Refresh Token 저장·JWT 블랙리스트** 및 위치 기반 캐시 전략을 정의한다.

**Input:**

- Step 1 설정 골격, PRD 4.1.3(Redis 활용), Step 4(JWT 인증 설계)

**Scope:**

- 포함: Redis 의존성·연결 설정, **Refresh Token Redis 저장**, **Access Token jti 블랙리스트**, 위치 캐시 키/TTL 정책 문서화
- 제외: 비즈니스 캐시 구현(반경 조회 등은 이후 단계)

**Instructions:**

- Redis 연결 설정(호스트·포트·비밀번호는 환경 변수)
- **Refresh Token 저장**: Redis 키 `refresh:{jti}`, TTL 7~30일 (RULE 6.5)
- **JWT 블랙리스트**: 로그아웃 시 `blacklist:{jti}` 등록, Access Token 만료시간 TTL
- (선택) 위치 기반 캐시용 키 네이밍·TTL 정책 문서화
- **로깅 (RULE 1.4.3)**: Redis 연결·토큰 저장 성공/실패 시 파라미터화 로깅, 비밀정보 제외

**Output Format:**

- 설정: `application-*.yml` 또는 환경 변수
- Redis: Refresh Token 저장·블랙리스트 조회 가능

**Constraints:**

- RULE 2.4(Stateless 지향), **6.1.7(Revocation)**, 6.5(Refresh Token Redis)
- Redis 비밀번호 등 비밀정보는 환경 변수 주입

**Done When:**

- 애플리케이션 기동 시 Redis에 연결되고, Refresh Token 저장·블랙리스트 조회가 가능하다.

**Duration:** 5일

**RULE Reference:** 2.4, 1.1, 1.4.3(로깅), **6.1.7, 6.5**

---

## Step 5.1 — DB·Redis 연결 Fallback 및 로깅

**Step Name:** DB·Redis 연결 Fallback 및 로깅

**Step Goal:** 서버 기동 시 DB(개발 H2/운영 MySQL) 및 Redis 연결 성공·실패를 로깅하고, 연결 실패 시에도 메인 서버는 기동하며 해당 기능을 비활성화한다.

**Input:**

- Step 1(환경별 설정), Step 5(Redis 연동·TokenStore)

**Scope:**

- 포함: DB·Redis 연결 검증 로깅(성공/실패), 연결 실패 시 서버 기동 유지, DB·Redis 미연결 시 해당 기능 비활성화
- 제외: 연결 복구 자동 재시도, Health Check 엔드포인트 상세 구현(Actuator 기본 유지)

**Instructions:**

- **DB 연결 로깅**: ApplicationRunner에서 DataSource ping 수행, 성공 시 INFO 로그, 실패 시 ERROR 로그 (RULE 1.4.3 파라미터화 로깅, 비밀정보 제외)
- **Redis 연결 로깅**: Redis ping 수행, 성공 시 INFO 로그, 실패 시 ERROR 로그, **실패해도 throw 하지 않고 서버 기동 계속**
- **연결 실패 시 서버 기동 유지**: DB·Redis 연결 실패가 애플리케이션 기동을 중단시키지 않도록 설정 (HikariCP `initialization-fail-timeout=-1` 등)
- **기능 비활성화**: DB 미연결 시 Repository 호출 시점에 예외 → GlobalExceptionHandler에서 503 처리; Redis 미연결 시 TokenStore NoOp fallback (로그인·토큰 갱신·블랙리스트 비활성)
- **로깅 (RULE 1.4.3)**: 연결 성공/실패 시 파라미터화 로깅, 비밀번호·토큰·연결 문자열 전체 출력 금지

**Output Format:**

- 설정: `application-*.yml` (DataSource/Redis fail-timeout 등)
- 코드: `config/` 내 DB·Redis 연결 검증·상태 관리, TokenStore fallback

**Constraints:**

- RULE 5.2.1(Fallback 전략), 5.3(기능 비활성화), 1.4.3(로깅), 1.1(비밀정보 로그 금지)
- DB·Redis 실패 시 예외 무시 금지 (RULE 2.2.2) — 로깅 후 기능 비활성 처리

**Done When:**

- DB·Redis 연결 성공 시 INFO, 실패 시 ERROR 로그가 출력된다.
- DB·Redis 미연결 상태에서도 서버가 기동되고, 해당 기능(DB 기반 API, 인증·토큰) 호출 시 503 또는 적절한 에러가 반환된다.

**Duration:** 2일

**RULE Reference:** 5.2.1(장애 대응·Fallback), 5.3(긴급 비활성화), 1.4.3(로깅), 1.1(비밀정보)

---

## Step 6 — 회원 도메인·가입·로그인 API

**Step Name:** 회원 도메인·가입·로그인 API

**Step Goal:** 회원가입·로그인 REST API를 구현하고, **JWT(Access+Refresh Token)** 를 발급·검증한다.

**Input:**

- Step 3(엔티티·Repository), Step 4(API 명세·JWT 인증 설계), Step 5(Redis·Refresh Token), Step 5.1(DB·Redis Fallback)

**Scope:**

- 포함: 회원가입(이메일/비밀번호), **로그인·JWT 발급**(Access 15분, Refresh Redis), 토큰 갱신·로그아웃, 중복 이메일 검증, 입력값 검증
- 제외: OAuth2, 이메일 인증, 비밀번호 찾기

**Instructions:**

- 회원가입 API: 이메일·비밀번호 등 필드 검증(@Valid), BCrypt/Argon2id 해싱, 중복 이메일 시 4xx·ErrorCode 반환
- **로그인 API**: 인증 성공 시 Access Token(15분 이하)+Refresh Token 발급, Refresh Token Redis 저장, 실패 시 401
- **토큰 갱신 API**: Cookie refreshToken → 새 Access Token 발급
- **로그아웃 API**: jti 블랙리스트 등록, Refresh Token Redis 삭제
- JWT: iss/aud/jti/exp 검증, alg allow-list(RS256/ES256), 민감정보 Payload 금지 (RULE 6.1)
- DTO·Service·Controller 계층 분리, 트랜잭션은 Service 계층
- **로깅 (RULE 1.4.2, 1.4.3)**: 인증 실패·권한 부족 시도 로깅, 민감 작업 감사 로그, 비밀번호·토큰 로그 출력 금지

**Output Format:**

- API: POST /api/members (가입), POST /api/auth/login, /refresh, /logout 등 명세와 일치
- 응답: 공통 ErrorResponse, 401/403 명확히 사용

**Constraints:**

- RULE 1.1(비밀정보 하드코딩 금지), 1.3(입력 검증), 1.2(인증 실패 401), **6.1~6.5(JWT)**

**Done When:**

- 회원가입·로그인 API가 동작하고, 로그인 시 JWT가 발급되며, 토큰 갱신·로그아웃이 정상 동작하고, 실패 시 401이 반환된다.

**Duration:** 6일

**RULE Reference:** 1.1, 1.2, 1.3, 1.4.2, 1.4.3(로깅)

---

## Step 7 — Spring Security·역할·관리자 인가

**Step Name:** Spring Security·역할·관리자 인가

**Step Goal:** URL별 인가를 적용하고, ROLE_USER/ROLE_ADMIN을 부여·검증하여 관리자 전용 경로를 보호한다.

**Input:**

- Step 6(회원 API), PRD 3.6(관리자 기능)

**Scope:**

- 포함: Spring Security 설정, 역할(USER/ADMIN) 부여·검증, /admin/\*\* 등 관리자 경로 403 처리, CORS allow-list
- 제외: JWT·OAuth2, 세부 관리자 기능 구현

**Instructions:**

- SecurityConfig: 인증·인가 규칙 정의, deny-by-default
- 로그인 사용자에게 ROLE_USER, 관리자 계정에 ROLE_ADMIN 부여
- **내부/관리자 API 분리 (RULE 2.1.3)**: `/api/admin/**` URL·보안 정책 분리, Admin API는 IP allow-list 또는 별도 인증 체계 고려
- /api/admin/\*\* 은 ROLE_ADMIN만 접근 허용, 미인가 시 403
- CORS: 허용 오리진을 allow-list로 제한(\* 금지)
- **로깅 (RULE 1.4.2, 1.4.3)**: 인가 실패(403) 시도 로깅, 파라미터화 로깅 사용

**Output Format:**

- 설정: SecurityConfig 등 `config/` 패키지
- 401(미인증)·403(권한 부족) 응답 명확

**Constraints:**

- RULE 1.2(인증·인가 분리, deny-by-default, 403 사용), 1.2.3(CORS 오설정 금지)
- **RULE 3.5**: 보안/권한 검사용 커스텀 AOP를 쓸 경우 횡단 관심사만, Pointcut 명시·@Order·문서화 준수

**Done When:**

- 비로그인 시 보호 리소스 401, 일반 사용자가 /admin/\*\* 접근 시 403이 반환되고, CORS가 allow-list로 동작한다.

**Duration:** 5일

**RULE Reference:** 1.2, **2.1.3(내부/Admin API 분리)**, **3.5**, 1.4.2, 1.4.3(로깅)

---

## Step 8 — 게시글(Post) CRUD·목록/상세 API

**Step Name:** 게시글(Post) CRUD·목록/상세 API

**Step Goal:** 일반 게시글의 목록·상세·작성·수정·삭제 API를 구현하고, 비로그인 조회/로그인 작성·수정·삭제 권한을 적용한다.

**Input:**

- Step 3(Post 엔티티·Repository), Step 4(API 명세), Step 7(인가)

**Scope:**

- 포함: Post 목록(페이징·검색)·상세, 작성/수정/삭제(로그인), 작성 위치(위도·경도 선택), 소유권 검증
- 제외: 이미지 업로드, Pin 연동 UI

**Instructions:**

- GET 목록/상세: 비로그인 허용, 페이징·검색 파라미터
- POST/PUT/DELETE: 로그인 필수, 수정·삭제 시 작성자 소유권 검증(IDOR 방지)
- 작성 시 제목·내용·작성 위치(위도·경도 선택) 저장
- 트랜잭션 경계는 Service, 읽기 전용 조회는 readOnly
- **트랜잭션·이벤트 (RULE 2.3.2)**: 이벤트 발행 시 트랜잭션 커밋 이후(`@TransactionalEventListener(AFTER_COMMIT)`)
- **로깅 (RULE 1.4.3)**: 주요 CRUD·소유권 검증 실패(IDOR) 시 파라미터화 로깅

**Output Format:**

- API: GET/POST/PUT/DELETE 명세와 일치, DTO 기반 요청/응답
- 403: 타인 글 수정/삭제 시도 시

**Constraints:**

- RULE 2.1(API 책임 단위), 2.3(트랜잭션 Service), 1.2(IDOR 방지)
- **RULE 3.5.5**: `@Transactional`은 Service 계층에만 선언(Controller·Repository 금지). 트랜잭션 AOP는 보조 역할만.

**Done When:**

- 목록·상세·작성·수정·삭제가 명세대로 동작하고, 타인 글 수정/삭제 시 403이 반환된다.

**Duration:** 6일

**RULE Reference:** 2.1, 2.3, **2.3.2(트랜잭션·이벤트)**, 1.2, **3.5.5**, 1.4.3(로깅)

---

## Step 9 — 이미지 게시글(ImagePost)·파일 업로드

**Step Name:** 이미지 게시글(ImagePost)·파일 업로드

**Step Goal:** 이미지 업로드(Multipart)와 이미지+텍스트 게시글 API를 구현하고, 파일 검증·저장 정책을 적용한다.

**Input:**

- Step 3(ImagePost 엔티티), Step 4(API 명세), Step 8(Post 권한 모델)

**Scope:**

- 포함: Multipart 업로드, 파일 타입·크기 검증, 저장 경로 분리·경로 traversal 방지, ImagePost CRUD
- 제외: CDN·이미지 리사이징·썸네일

**Instructions:**

- 이미지 업로드: 허용 MIME·최대 크기 제한, 파일명 정규화·경로 traversal 방지
- ImagePost 생성/수정/삭제 API, 저장 경로는 설정·환경 변수로 분리
- Post와 동일한 권한 원칙(작성자만 수정/삭제)
- **로깅 (RULE 1.4.3)**: 파일 업로드·검증 실패·저장 실패 시 파라미터화 로깅, 파일 경로·사용자 정보 민감처리

**Output Format:**

- API: ImagePost CRUD, 이미지 URL 또는 식별자 응답
- 저장: 외부 저장 경로 또는 스토리지 정책 문서화

**Constraints:**

- RULE 1.3(입력 검증), ASVS V12(파일·리소스 보안)

**Done When:**

- 이미지 업로드 및 ImagePost CRUD가 동작하고, 허용되지 않은 파일 타입·크기에서는 4xx가 반환된다.

**Duration:** 5일

**RULE Reference:** 1.3, ASVS V12, 1.4.3(로깅)

---

## Step 10 — Pin·Location 도메인·API

**Step Name:** Pin·Location 도메인·API

**Step Goal:** Pin CRUD API와 Location(위도/경도) 연동, Pin↔Post/ImagePost 연관 구조를 구현한다.

**Input:**

- Step 3(Pin·Location), Step 4(API 명세), Step 8·9(Post·ImagePost)

**Scope:**

- 포함: Pin 생성/수정/삭제/목록(사용자별), Location VO 활용, Pin과 Post/ImagePost 연관 설계·저장
- 제외: 지도 UI, 반경 조회

**Instructions:**

- Pin API: 위치(위도·경도)·설명 등 필드, 사용자별 목록
- Pin과 Post/ImagePost 연관(1:N 또는 N:M) 설계·엔티티 반영
- DTO 기반 요청/응답, 소유권 검증
- **로깅 (RULE 1.4.3)**: 주요 CRUD·소유권 검증 실패 시 파라미터화 로깅

**Output Format:**

- API: Pin CRUD, 연관된 Post/ImagePost 식별자 또는 요약 포함 가능
- 엔티티: 연관 관계 반영

**Constraints:**

- RULE 3.1(계층 분리), 4.1(DTO 기반)

**Done When:**

- Pin CRUD와 Post/ImagePost 연관이 저장·조회되며, API 명세와 일치한다.

**Duration:** 5일

**RULE Reference:** 3.1, 4.1, 1.4.3(로깅)

---

## Step 11 — 지도 API 추상화·반경 조회 서비스

**Step Name:** 지도 API 추상화·반경 조회 서비스

**Step Goal:** 지도 API를 추상화 레이어로 설계하고, 현재 위치 기반 반경 내 Pin·게시글 조회 API를 제공한다.

**Input:**

- PRD 2.2(지도 추상화), Step 10(Pin·Post 연관), Step 5(Redis 캐시 정책)

**Scope:**

- 포함: 지도 서비스 인터페이스·1개 이상 구현체(Kakao/Naver/Google), 반경 내 Pin·게시글 조회 API, (선택) Redis 캐시
- 제외: 지도 프론트 UI, 경로/거리 계산 상세

**Instructions:**

- 지도 API 호출을 인터페이스로 추상화, 구현체는 프로퍼티로 선택
- 반경(위도·경도·반경 km 등) 조건으로 Pin·Post/ImagePost 조회 API 구현
- 외부 지도 API 호출 시 Timeout·Retry 정책 명시
- (선택) 반경 조회 결과 Redis 캐시
- **로깅 (RULE 1.4.3)**: 외부 API 호출·실패·재시도 시 파라미터화 로깅, 민감정보 제외

**Output Format:**

- 서비스: `map/` 또는 `location/` 패키지에 인터페이스·구현체
- API: GET /api/pins/nearby 또는 /api/posts/nearby 등 반경 조회

**Constraints:**

- RULE 3.4(외부 호출 Timeout), 3.2(프레임워크·외부 API 과의존 금지)
- **RULE 3.5.8**: 반경 조회·대량 호출 등 성능 민감 구간에 AOP(로깅·메트릭) 적용 시 **적용 전 성능 테스트 필수**

**Done When:**

- 지도 API 구현체를 교체 가능하고, 반경 조회 API가 위치·반경 파라미터에 따라 Pin·게시글을 반환한다.

**Duration:** 6일

**RULE Reference:** 3.4, 3.2, **3.5.8**, 1.4.3(로깅)

---

## Step 12 — 지도 웹 UI (Thymeleaf)·Pin 표시

**Step Name:** 지도 웹 UI (Thymeleaf)·Pin 표시

**Step Goal:** 메인 지도 화면을 Thymeleaf로 구현하고, 사용자 위치·반경 내 Pin을 표시하며, Pin 클릭 시 관련 게시글 목록·상세로 이동한다.

**Input:**

- Step 11(반경 조회 API), PRD 3.3(지도 핵심 기능)

**Scope:**

- 포함: Thymeleaf 지도 메인 페이지, GPS/위치 권한·현재 위치 표시, 반경 내 Pin 마커, Pin 클릭 시 게시글 목록·상세 링크
- 제외: 경로 시각화, Flutter 모바일

**Instructions:**

- 지도 메인 페이지: 선택한 지도 API(카카오/네이버/구글) JS 연동
- 사용자 위치 획득·지도 중심 설정, 반경 내 Pin 마커 표시
- Pin 클릭 시 해당 Pin의 게시글 목록·상세 링크 제공
- 사용자 입력 표시 시 XSS 방지(이스케이프·dangerouslySetInnerHTML 금지)
- **로깅 (RULE 1.4.3)**: 지도 API 호출·위치 권한 거부 등 클라이언트 이벤트 서버 전달 시 파라미터화 로깅

**Output Format:**

- 뷰: `templates/` 내 Thymeleaf, 정적 리소스 `static/`
- 지도·Pin 표시 동작 확인 가능

**Constraints:**

- RULE 1.5.6(XSS 방지), 1.3(클라이언트 검증만 믿지 않기)

**Done When:**

- 메인 지도에서 현재 위치와 반경 내 Pin이 표시되고, Pin 클릭 시 관련 게시글 목록·상세로 이동한다.

**Duration:** 6일

**RULE Reference:** 1.5.6, 1.3, 1.4.3(로깅)

---

## Step 13 — 지도-게시글 연동·경로/거리

**Step Name:** 지도-게시글 연동·경로/거리

**Step Goal:** 게시글 작성 시 위치/Pin 연결, 게시글 상세에 지도 위치 표시, 사용자 위치→목적지 거리·경로 시각화를 구현한다.

**Input:**

- Step 8·10(Post·Pin), Step 11·12(지도 서비스·웹 UI)

**Scope:**

- 포함: 글 작성 시 위치/Pin 선택·연결, 상세 페이지 지도에 위치 표시, 거리 계산·경로 시각화(출발/도착)
- 제외: 실시간 경로 추적, 모바일 전용 UX

**Instructions:**

- 게시글 작성 폼: 위치(위도·경도) 또는 기존 Pin 선택
- 게시글 상세: 지도에 해당 위치·Pin 표시
- 거리·경로 API 또는 클라이언트 계산: 사용자 위치→선택 위치 거리, (선택) 경로 폴리라인
- 출발/도착 지점 설정 UI
- **로깅 (RULE 1.4.3)**: 거리/경로 계산·외부 API 호출 시 파라미터화 로깅

**Output Format:**

- 웹: 작성·상세 화면에 지도·위치·경로 연동
- API: 거리/경로 필요 시 명세에 맞는 엔드포인트

**Constraints:**

- RULE 2.1(API 책임 단위 유지)

**Done When:**

- 글 작성 시 위치/Pin이 연결되고, 상세에서 지도에 표시되며, 거리·경로(출발/도착)가 표시된다.

**Duration:** 5일

**RULE Reference:** 2.1, 1.4.3(로깅)

---

## Step 14 — 마이페이지·About 페이지

**Step Name:** 마이페이지·About 페이지

**Step Goal:** 내 게시글·이미지 게시글·Pin 목록, 개인정보 수정, About 페이지를 제공한다.

**Input:**

- Step 8·9·10(Post·ImagePost·Pin API), Step 7(인가)

**Scope:**

- 포함: 내 게시글·이미지 게시글·Pin 목록(페이징), 개인정보 수정(본인만), About(서비스 소개·컨셉·기술 스택)
- 제외: 프로필 이미지·활동 통계

**Instructions:**

- 마이페이지: 로그인 사용자 본인 데이터만 조회(리소스 소유권 검증)
- 내 게시글·ImagePost·Pin 목록 API·화면, 페이징
- 개인정보 수정 API·화면(이메일·닉네임 등), 본인 인증 후 수정
- About: 정적 또는 단순 뷰로 서비스 소개·지도 SNS 컨셉·기술 스택
- **로깅 (RULE 1.4.3)**: 개인정보 수정 등 민감 작업 감사 로그, 파라미터화 로깅

**Output Format:**

- API: GET /api/me/posts, /api/me/image-posts, /api/me/pins 등
- 뷰: 마이페이지·About Thymeleaf

**Constraints:**

- RULE 1.2(리소스 소유권 검증)

**Done When:**

- 로그인 사용자가 자신의 글·Pin 목록을 보고, 개인정보를 수정할 수 있으며, About 페이지가 노출된다.

**Duration:** 4일

**RULE Reference:** 1.2, 1.4.3(로깅)

---

## Step 15 — 관리자 회원 관리

**Step Name:** 관리자 회원 관리

**Step Goal:** ROLE_ADMIN 전용으로 전체 회원 조회·추가·수정·삭제 기능을 제공한다.

**Input:**

- Step 6·7(회원·인가), PRD 3.6.1

**Scope:**

- 포함: 회원 목록(페이징·검색), 회원 추가/수정/삭제 API·Thymeleaf 화면
- 제외: 대량 삭제·역할 일괄 변경

**Instructions:**

- /admin/\*\* 하위 회원 관리 API·화면
- 목록: 페이징·검색(이메일·이름 등)
- 추가: 관리자에 의한 회원 등록
- 수정: 프로필·역할 등
- 삭제: 회원 탈퇴/삭제 처리
- 모든 요청에 ROLE_ADMIN 검증
- **로깅 (RULE 1.4.2, 1.4.3)**: 관리자 회원 추가/수정/삭제 등 민감 작업 감사 로그, 파라미터화 로깅

**Output Format:**

- API: GET/POST/PUT/DELETE /admin/members 등
- 뷰: 관리자 전용 Thymeleaf

**Constraints:**

- RULE 1.2(최소 권한, 관리자만 접근), PRD 3.6

**Done When:**

- 관리자만 회원 목록·추가·수정·삭제에 접근 가능하고, 일반 사용자는 403이다.

**Duration:** 5일

**RULE Reference:** 1.2, PRD 3.6, 1.4.2, 1.4.3(로깅)

---

## Step 16 — 관리자 게시물 관리·공지

**Step Name:** 관리자 게시물 관리·공지

**Step Goal:** 관리자가 전체 게시글(일반·이미지)을 조회·수정·삭제하고, 공지 등록/해제 및 공지 상단 노출을 구현한다.

**Input:**

- Step 8·9(Post·ImagePost), PRD 3.6.2

**Scope:**

- 포함: 게시글 목록(페이징·검색), 수정/삭제, 공지 여부 필드·공지 상단 노출
- 제외: 대량 삭제·스팸 처리 자동화

**Instructions:**

- 관리자용 게시글 목록 API·화면(일반·이미지 통합 또는 분리)
- 수정/삭제: 관리자 권한으로 타인 글도 가능
- Post(및 필요 시 ImagePost)에 공지 플래그 추가, 공지 상단 노출 로직(목록 정렬·필터)
- 공지 등록/해제 API·화면
- **로깅 (RULE 1.4.2, 1.4.3)**: 관리자 게시물 수정/삭제·공지 등록 등 민감 작업 감사 로그

**Output Format:**

- API: GET/PUT/DELETE /admin/posts 등, 공지 플래그 PATCH 등
- 뷰: 관리자 게시물 관리·공지 설정 화면

**Constraints:**

- RULE 1.2(IDOR·권한 검증, 관리자만 접근)

**Done When:**

- 관리자가 전체 게시글을 조회·수정·삭제하고, 공지를 등록/해제할 수 있으며, 목록에서 공지가 상단에 노출된다.

**Duration:** 4일

**RULE Reference:** 1.2, 1.4.3(로깅)

---

## Step 17 — 관리자 통계 (가입/로그인/글)

**Step Name:** 관리자 통계 (가입/로그인/글)

**Step Goal:** 회원 가입·로그인 통계와 기간별 글 통계(일/주/월/분기/년)를 API·대시보드로 제공한다.

**Input:**

- Step 6·8·9(회원·게시글), PRD 3.6.3, 3.6.4

**Scope:**

- 포함: 가입 통계(기간별), 로그인 통계(기간별·활성 사용자), 글 통계(일/주/월/분기/년), 기간 필터·테이블·차트
- 제외: 실시간 대시보드·외부 연동

**Instructions:**

- 가입 통계: 기간별 가입자 수 API·화면
- 로그인 통계: 기간별 로그인 수·활성 사용자(세션/로그 이력 기반)
- 글 통계: 일/주/월/분기/년 단위 집계 API·화면, 기간 선택
- (선택) 차트 라이브러리로 시각화
- 통계 조회는 readOnly 트랜잭션, 쿼리 최적화
- **로깅 (RULE 1.4.3)**: 통계 조회·대량 쿼리 시 파라미터화 로깅, 무의미한 반복 로그 금지

**Output Format:**

- API: GET /admin/stats/signup, /admin/stats/login, /admin/stats/posts 등
- 뷰: 통계 대시보드(테이블·차트)

**Constraints:**

- RULE 2.3(읽기 전용 readOnly), 통계 쿼리 부하 고려

**Done When:**

- 관리자가 기간을 선택해 가입·로그인·글 통계를 조회하고, (선택) 차트로 확인할 수 있다.

**Duration:** 5일

**RULE Reference:** 2.3, 1.4.3(로깅)

---

## Step 18 — 웹 보안·CORS·Rate Limiting 점검

**Step Name:** 웹 보안·CORS·Rate Limiting 점검

**Step Goal:** RULE 기준으로 보안을 점검하고, CORS·Rate Limiting을 적용한다.

**Input:**

- RULE 1(보안), 부록 체크리스트, 전 단계 완료 코드

**Scope:**

- 포함: CORS allow-list 최종 점검, 로그인/가입 등 Rate Limiting, 비밀정보·에러 메시지·기본값 노출 재점검, RULE 부록 체크리스트 수행
- 제외: 침투 테스트·외부 감사

**Instructions:**

- CORS: 허용 오리진을 allow-list로 제한, \* 사용 금지
- 로그인·회원가입·토큰 갱신 등에 Rate Limiting 적용
- 로그·에러 응답에 비밀정보·스택 트레이스·내부 경로 미포함 확인
- 기본 계정·비밀번호·불필요 엔드포인트 노출 제거 확인
- **긴급 비활성화 (RULE 5.3)**: 핵심 기능 Feature Toggle 또는 Kill Switch 고려 여부 점검
- RULE 부록 B·C 체크리스트 항목 점검·보완
- **로깅 점검 (RULE 1.4.1, 1.4.2, 1.4.3)**: 비밀정보·스택 트레이스 로그 미포함, 인증/인가 실패 로깅 확인, 파라미터화·레벨 준수

**Output Format:**

- 설정: CORS·Rate Limiting 설정 코드
- 점검 결과: 체크리스트 또는 간단 보고(문서·이슈)

**Constraints:**

- RULE 1.2.3(CORS), 1.5.4(가용성), 1.6(보안 설정)

**Done When:**

- CORS·Rate Limiting이 적용되어 있고, RULE 체크리스트 항목이 충족되었다고 확인된다.

**Duration:** 3일

**RULE Reference:** 1.2.3, 1.5.4, 1.6, **5.3(긴급 비활성화)**, 1.4.1, 1.4.2, 1.4.3(로깅)

---

## Step 19 — 테스트·문서(Swagger)·RULE 체크

**Step Name:** 테스트·문서(Swagger)·RULE 체크

**Step Goal:** 핵심 비즈니스 로직 단위/슬라이스/통합 테스트를 **RULE 4.2.2**에 맞게 추가하고, Swagger를 정리하며, RULE 준수를 최종 확인한다.

**Input:**

- Step 2~17 완료 코드, RULE 4.2·4.2.2·4.3

**Scope:**

- 포함: Service 계층 단위 테스트, Controller 슬라이스 테스트(@WebMvcTest), (선택) Repository 슬라이스·통합 테스트, Swagger/OpenAPI 최종 정리, RULE 준수 확인
- 제외: E2E·부하 테스트

**Instructions:**

- **테스트 작성 (RULE 4.2.2, 4.2.1.1, 1.2.4 준수)**
  - **Given-When-Then 패턴** 및 `// given`, `// when`, `// then` 주석 필수
  - **AssertJ** 사용, **BDDMockito** 권장(given/willReturn), **Mockito** 목 객체
  - 테스트 메서드 이름: BDD 스타일 (`[메서드명]_[상황]_should[결과]` 등), `@DisplayName` 필수
  - 단위 테스트: `@ExtendWith(MockitoExtension.class)` · Controller: `@WebMvcTest`+`@MockBean` · Repository: `@DataJpaTest` · 통합: `@SpringBootTest`(최소화)
  - 예외 테스트: `assertThatThrownBy()` 사용, 한 테스트 메서드 = 한 시나리오
  - **테스트 결정성 (4.2.1.1)**: `System.currentTimeMillis()`, `LocalDateTime.now()` 직접 사용 금지, `Clock`/`TimeProvider` 추상화. 랜덤값은 고정 시드 또는 Stub
  - **인증·인가 테스트 (1.2.4)**: 보호 API는 401(미인증)·403(권한 부족) 테스트 최소 1개 이상 필수
- 핵심 Service 메서드 단위 테스트, (선택) Controller 슬라이스·REST API 통합 테스트(Testcontainers 등)
- Swagger: 모든 공개 API 설명·요청/응답 예시 정리
- RULE 문서 대비 누락·위반 사항 정리·수정
- **AOP RULE 3.5 점검**: 적용된 AOP에 대해 Pointcut(명시적·Annotation 기반), @Order, 예외 재throw, 상태 변경 미사용 확인; AOP 문서(적용 대상·목적·실행 시점·예외 정책·제거 시 영향도) 존재 여부 확인
- **로깅 RULE 1.4.3 점검**: SLF4J 사용, 파라미터화 로깅, 민감정보 미출력, System.out/err 미사용, 예외 catch 후 로그 필수

**Output Format:**

- 테스트: `src/test/` JUnit 5 + AssertJ + Mockito, RULE 4.2.2 템플릿 준수
- 문서: Swagger UI, (선택) RULE 체크 결과 요약, **AOP 문서(README 또는 doc/)**

**Constraints:**

- RULE 4.2(테스트 없는 핵심 로직 배포 금지, 외부 환경 의존 최소화), **4.2.2(테스트 코드 작성 규칙)** 준수
- RULE 4.3(API 문서화), **RULE 3.5**: AOP는 횡단 관심사 전용, 문서화 필수

**Done When:**

- 핵심 로직에 **RULE 4.2.2** 준수 테스트가 있고, Swagger가 최신 API와 일치하며, RULE 체크가 완료되었다.
- **테스트에 Given-When-Then·AssertJ·BDDMockito·@DisplayName·BDD 스타일 이름이 적용되어 있다.**
- **적용된 AOP가 RULE 3.5를 만족하고, AOP 문서가 존재한다.**

**Duration:** 5일

**RULE Reference:** 4.2, **4.2.2(테스트 규칙)**, **4.2.1.1(테스트 결정성)**, **1.2.4(인증·인가 테스트)**, 4.3, **3.5**, 1.4.3(로깅)

---

## Step 20 — 배포 설정·최종 점검

**Step Name:** 배포 설정·최종 점검

**Step Goal:** 운영(MySQL, Redis) 설정과 배포 가이드를 완성하고, Flutter 연동 API 구조 예시 문서를 작성한다.

**Input:**

- Step 1(환경별 설정), Step 5(Redis), 전체 API·인증 설계

**Scope:**

- 포함: MySQL·Redis 운영용 설정, 환경 변수·Secret 관리 가이드, 배포 절차·헬스체크, Flutter REST·지도 연동 구조 예시 문서
- 제외: CI/CD 파이프라인 구현·컨테이너 이미지 빌드

**Instructions:**

- application-prod.yml 등 운영 설정: DB·Redis·서버 URL 등 환경 변수 참조로 통일
- 배포 가이드: 필요 런타임(Java 21, Redis, MySQL), 환경 변수 목록, 기동·헬스체크 방법
- **긴급 비활성화 (RULE 5.3)**: Feature Toggle 또는 Kill Switch 적용 여부·운영 방법 문서화
- Flutter 연동: REST API 호출 구조·**인증(JWT Bearer, Refresh Token Secure Storage)** 처리·지도 SDK 연동 예시를 문서로 정리
- **로깅 (RULE 1.4.3)**: 운영 환경 로그 레벨·파일 경로·롤링 정책이 application-prod.yml에 반영되었는지 확인

**Output Format:**

- 설정: `application-prod.yml`, (선택) `docker-compose` 또는 배포 스크립트
- 문서: `doc/` 내 배포 가이드, Flutter 연동 API 구조 예시

**Constraints:**

- RULE 1.1(비밀정보 외부 주입), PRD 7(운영 기준)

**Done When:**

- 운영 환경 설정과 배포 가이드가 준비되었고, Flutter 연동 예시 문서가 존재한다.

**Duration:** 5일

**RULE Reference:** 1.1, PRD 7, **5.3(긴급 비활성화)**, 1.4.3(로깅)

---

## RULE 참조 요약

| 영역      | 참조                                                                                                                      |
| --------- | ------------------------------------------------------------------------------------------------------------------------- |
| 보안      | RULE 1 (비밀정보, 인증·인가, **1.2.4 인증·인가 테스트**, 입력검증, 로그 1.4.1~1.4.3, 암호화, 설정, 공급망)                |
| 기능      | RULE 2 (API 설계, **2.1.3 내부/Admin API 분리**, 예외, **2.2.6 Checked Exception 금지**, **2.3.2 트랜잭션·이벤트**, 상태) |
| 기술      | RULE 3 (계층, ORM/QueryDSL, 통신)                                                                                         |
| 품질      | RULE 4 (코드, **테스트 4.2·4.2.2·4.2.1.1 결정성**, 문서)                                                                  |
| 운영      | RULE 5 (설정 분리, 장애 대비, **5.3 긴급 비활성화**)                                                                      |
| 인증/토큰 | RULE 6 (JWT, RSA, CIA, OAuth/OIDC)                                                                                        |

---

## 진행 추적 (체크리스트)

| Step | 내용                                   | 완료 |
| ---- | -------------------------------------- | ---- |
| 1    | 프로젝트 셋업·아키텍처·ERD             | ☑    |
| 2    | 공통 인프라 (예외·Validation·보안·AOP) | ☑    |
| 3    | 패키지·엔티티·Repository               | ☑    |
| 4    | REST API 명세·인증 설계                | ☑    |
| 5    | Redis·JWT·캐시                         | ☑    |
| 5.1  | DB·Redis 연결 Fallback 및 로깅         | ☑    |
| 6    | 회원 가입·로그인                       | ☑    |
| 7    | Spring Security·역할                   | ☑    |
| 8    | Post CRUD                              | ☑    |
| 9    | ImagePost·파일 업로드                  | ☑    |
| 10   | Pin·Location·API                       | ☑    |
| 11   | 지도 추상화·반경 조회                  | ☑    |
| 12   | 지도 웹 UI·Pin 표시                    | ☑    |
| 13   | 지도-게시글 연동·경로/거리             | ☑    |
| 14   | 마이페이지·About                       | ☐    |
| 15   | 관리자 회원 관리                       | ☐    |
| 16   | 관리자 게시물·공지                     | ☐    |
| 17   | 관리자 통계                            | ☐    |
| 18   | 보안·CORS·Rate Limiting                | ☐    |
| 19   | 테스트·문서·RULE 체크                  | ☐    |
| 20   | 배포 설정·최종 점검                    | ☐    |

---

> **문서 버전**: 1.0.1
> **기준**: PRD 1.0.0, RULE Book (4.2.2 테스트 규칙)
> **최종 업데이트**: 2026-02-05
