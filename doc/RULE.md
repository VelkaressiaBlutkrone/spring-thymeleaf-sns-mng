# 프로젝트 개발 규칙 (RULE Book)

> **이 규칙은 프로젝트의 도메인, 규모, 일정과 무관하게 반드시 준수한다.**
> **예외가 필요한 경우 반드시 기술 리더의 승인과 문서화를 거친다.**
> **규칙 위반은 코드 리뷰에서 반려 사유가 된다.**

### 문서 사용 가이드

- 본 문서는 **목차·요약**만 제공한다. **상세 규칙**은 `doc/rules/` 폴더 내 챕터별 md 파일을 참조한다.
- **일상 참조** 시: [부록 C 체크리스트](rules/appendix-c-checklist.md)와 아래 목차로 필요한 챕터만 연다.
- **레벨 표기**: [MUST] = 위반 시 즉시 반려, [SHOULD] = 원칙·예외 시 문서화, [RECOMMENDED] = 권장. 개별 항목이 다를 경우 해당 항목 옆에 명시.

### 운영 원칙 (v1.0.5)

> **보안과 가용성이 충돌할 경우**, 최우선적으로 **보안을 유지하면서 가용성을 확보할 수 있는 대안**(예: Grace Period)을 설계한다.
> **가용성을 위해 보안을 포기하는 결정**은 기술 리더의 서면 승인이 없는 한 절대 금지한다.

---

## RULE 레벨

| 레벨            | 의미                 |
| --------------- | -------------------- |
| **MUST**        | 위반 시 즉시 반려    |
| **SHOULD**      | 원칙, 예외 시 문서화 |
| **RECOMMENDED** | 권장                 |

---

## 분리된 규칙 문서 목록 (doc/rules/)

| 문서 | 설명 |
|------|------|
| [01-security.md](rules/01-security.md) | 보안: 비밀정보·인증인가·입력검증·로그·암호화·Rate Limiting·AI 보안 등 [MUST] |
| [02-function.md](rules/02-function.md) | 기능: API 설계·예외 처리·트랜잭션·상태 관리·SOLID [MUST] |
| [03-technical.md](rules/03-technical.md) | 기술: 아키텍처·프레임워크·ORM/QueryDSL·통신·AOP·외부 라이브러리 [SHOULD] |
| [04-quality.md](rules/04-quality.md) | 품질: 코드·테스트·문서·주석 [MUST/SHOULD] |
| [05-operation.md](rules/05-operation.md) | 운영: 설정 분리·장애 대비·긴급 비활성화 [SHOULD] |
| [06-auth-token.md](rules/06-auth-token.md) | 인증/토큰: JWT·RSA·인가·OAuth/OIDC·토큰 무효화 [MUST] |
| [07-platform.md](rules/07-platform.md) | 플랫폼: Spring Boot·React·Flutter 구현 가이드 |
| [08-javascript.md](rules/08-javascript.md) | 프론트엔드 JavaScript: 변수·함수·비동기·에러 처리·네이밍 (7.2 React 하위) |
| [09-observability.md](rules/09-observability.md) | Observability: 구조화 로깅·traceId/spanId·OpenTelemetry [MUST/SHOULD] |
| [10-container-k8s.md](rules/10-container-k8s.md) | Container & Kubernetes 보안 (해당 환경 사용 시만 적용) |
| [11-data-sovereignty.md](rules/11-data-sovereignty.md) | 데이터 주권·개인정보: 최소화·파기·탈퇴·감사·마스킹 [MUST] |
| [appendix-a-asvs.md](rules/appendix-a-asvs.md) | 부록 A: ASVS 5.0 챕터별 대응 (문제→대책) |
| [appendix-b-owasp.md](rules/appendix-b-owasp.md) | 부록 B: OWASP Top 10 2025 매핑표 |
| [appendix-c-checklist.md](rules/appendix-c-checklist.md) | 부록 C: 규칙 준수 체크리스트 (일상 참조용) |

---

## 핵심 규칙 요약 (1페이지)

### 보안 [MUST]

- 비밀정보: 소스 하드코딩 금지, 외부 주입만 허용. Secrets 자동 로테이션·Grace Period·무중단 계획 필수.
- 인증·인가: 401/403 명확 구분, IDOR·CORS·SSRF 방어. 보호 API는 인증/인가 테스트 최소 1개 필수.
- 입력: 모든 외부 입력 불신, Controller 검증 + Service 비즈니스 검증. SQL/NoSQL 파라미터 바인딩만.
- 로그: SLF4J, `{}` 파라미터화 로깅, 민감정보·스택트레이스 사용자 반환 금지.
- 통신: HTTPS(TLS 1.3) 필수. 애플리케이션 레벨 암호화는 특수 케이스만.
- Rate Limiting: 모든 공개 API 적용, 429 + Retry-After. 수치는 규모·트래픽 분석 후 조정.

### 기능 [MUST]

- API: HTTP Method 준수, 계층형 자원 URI, 내부/Admin API 분리.
- 예외: 공통 예외 체계(BusinessException + ErrorCode), GlobalExceptionHandler. Controller/Service에서 IllegalArgumentException 등 직접 사용 금지(도메인 내부는 예외 허용). Checked 사용 시 rollbackFor 명시.
- 트랜잭션: Service 계층, 읽기 전용은 readOnly. 트랜잭션 내부에서 외부 이벤트 발행 금지.
- SOLID: DIP·OCP 최우선. 위반 시 PR에 이유 설명.

### 기술 [SHOULD]

- 계층: Controller → Service → Domain/Repository 단방향. N+1 대응, 엔티티 직접 반환 금지. QueryDSL 사용 시에만 해당 규칙 적용.
- AOP: 횡단 관심사 전용, 비즈니스 로직 금지. 상세는 rules/aop-guide.md 분리 권장.
- 외부 호출: Timeout·Retry 정책 명시. 라이브러리 버전은 TECH-STACK.md / libs.versions.toml 관리.

### 품질 [MUST/SHOULD]

- 핵심 로직 테스트 필수. Given-When-Then, AssertJ, BDDMockito. 메서드명 영문 + @DisplayName 한글 허용.
- public API Javadoc 필수에 가깝게, 한글 주석 기본.

### 인증/토큰 [MUST]

- JWT: 모든 서비스에서 서명·클레임 검증. alg allow-list, iss/aud 검증. Access 15분 이하, jti·Revocation. 웹은 HttpOnly 쿠키, localStorage 금지.
- OAuth/OIDC: Authorization Code + PKCE. Implicit/Password Grant 금지. Redirect URI allow-list, State/Nonce 필수.

### Observability [MUST/SHOULD]

- MUST: 구조화 로깅 + traceId/spanId MDC. SHOULD: OpenTelemetry 3 signals, 샘플링 전략 명시. 100% 샘플링 운영 금지.

### Container/K8s (해당 시만)

- 이미지: 최소 베이스, 태그 고정, Trivy/Grype 스캔 필수. PSS restricted, SecurityContext, NetworkPolicy deny-all.

### 데이터 주권 [MUST]

- 물리 삭제 원칙, 탈퇴 즉시 익명화·삭제, 로그·캐시·백업 연관 데이터 삭제. DB 직접 접근 통제·접근 로그 2년 보존. PII 마스킹·검색 가능 암호화.

---

> **마지막 업데이트**: 2026-02-09  
> **버전**: 1.0.9 (챕터별 rules/ 분리 완료)
