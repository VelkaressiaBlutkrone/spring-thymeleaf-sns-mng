# 부록 C: 규칙 준수 체크리스트

> 일상 참조용 체크리스트. 상세 규칙은 [RULE.md](../RULE.md) 목차 또는 rules/ 내 해당 문서 참조.

| 카테고리             | 체크 항목                                                                        | 필수 |
| -------------------- | -------------------------------------------------------------------------------- | ---- |
| 보안                 | 비밀정보 외부 주입 방식 사용                                                     | ✅   |
| 보안                 | 인증 실패 시 401 반환                                                            | ✅   |
| 보안                 | 권한 부족 시 403 반환                                                            | ✅   |
| 보안                 | 입력값 검증 적용                                                                 | ✅   |
| 보안                 | 로그에 민감정보 미포함                                                           | ✅   |
| 보안 (Secrets)       | 자동 로테이션 필수, 장기 정적 secrets 180일 초과 금지 (RULE 1.1.3~1.1.4)         | ✅   |
| 보안 (Secrets)       | Grace Period·지수 백오프·Rotation Health Check·긴급 롤백 절차 (RULE 1.1.3.1)     | ✅   |
| 데이터 주권 (11.x)   | 물리 삭제 원칙, 탈퇴 즉시 익명화·삭제, 연관 데이터(로그·캐시·백업) 삭제          | ✅   |
| 데이터 주권 (11.x)   | DB 직접 접근 통제·결재, 접근 로그 2년 보존, PII 마스킹·검색 가능 암호화          | ✅   |
| 보안 (Rate Limiting) | 공개 API Rate Limiting 적용, 429 + Retry-After 반환 (RULE 1.9)                   | ✅   |
| 보안 (로깅)          | SLF4J 사용, 파라미터화 로깅 `{}`, 로그 레벨 준수 (RULE 1.4.3)                    | ✅   |
| Observability (9.x)  | OpenTelemetry, traceId/spanId MDC 삽입, 100% 샘플링 운영 금지                    | ✅   |
| Container/K8s (10.x) | Trivy/Grype 이미지 스캔, PSS restricted, SecurityContext, NetworkPolicy deny-all | ✅   |
| 기능                 | HTTP Method 의미 준수                                                            | ✅   |
| 기능                 | 공통 예외 체계 사용                                                              | ✅   |
| 기능                 | 트랜잭션 경계 Service 계층                                                       | ✅   |
| 기능 (SOLID 2.5)     | DIP·OCP 최우선, 위반 시 PR에 이유 설명 (RULE 2.5)                                | ✅   |
| 기술                 | 계층 간 단방향 의존성                                                            | ✅   |
| 기술                 | N+1 문제 대응                                                                    | ✅   |
| 기술                 | 엔티티 직접 반환 금지                                                            | ✅   |
| 기술                 | 외부 호출 Timeout 설정                                                           | ✅   |
| 기술 (AOP)           | 횡단 관심사 전용, 비즈니스 로직·상태 변경 금지                                   | ✅   |
| 기술 (AOP)           | Pointcut 명시적·Annotation 기반, @Transactional Service만                        | ✅   |
| 기술 (AOP)           | AOP 예외 기록 후 재throw, 다중 AOP 시 @Order 명시                                | ✅   |
| 기술 (AOP)           | AOP 추가 시 문서화(Pointcut·목적·영향도)                                         | ✅   |
| 품질                 | 핵심 로직 테스트 존재                                                            | ✅   |
| 품질 (테스트)        | Given-When-Then 패턴, // given / when / then 주석 필수 (RULE 4.2.2)              | ✅   |
| 품질 (테스트)        | AssertJ 사용, BDDMockito 권장, @DisplayName 필수                                 | ✅   |
| 품질 (테스트)        | 테스트 메서드 이름 BDD 스타일 (메서드명\_상황\_should결과)                       | ✅   |
| 품질                 | API 문서화 (Swagger)                                                             | ✅   |
| 품질 (주석)          | public API Javadoc 필수, 한글 주석 기본 (RULE 4.4)                               | ✅   |
| 운영                 | 환경별 설정 분리                                                                 | ✅   |
| 운영                 | Fallback 전략 정의                                                               | ✅   |
| Spring Boot          | DB 등 민감정보 환경변수 주입                                                     | ✅   |
| Spring Boot          | GlobalExceptionHandler로 예외 통일 반환                                          | ✅   |
| React                | dangerouslySetInnerHTML 금지, 토큰 메모리/HttpOnly                               | ✅   |
| React                | 공통 Axios 인스턴스 + 토큰 인터셉터                                              | ✅   |
| JavaScript (8.x)     | var 금지, const > let, 전역 변수 금지                                            | ✅   |
| JavaScript (8.x)     | async/await 기본, .then 체인·콜백 지옥 금지                                      | ✅   |
| JavaScript (8.x)     | async 함수 내 try-catch, 전역 에러 핸들링(ErrorBoundary 등)                      | ✅   |
| Flutter              | SharedPreferences 대신 flutter_secure_storage                                    | ✅   |
| Flutter              | 비동기 try-catch + 사용자 친화적 에러 메시지                                     | ✅   |
| OWASP A01            | IDOR·SSRF·CORS 검증                                                              | ✅   |
| OWASP A02            | 보안 설정(기본값·에러 정보 미노출)                                               | ✅   |
| OWASP A03            | 의존성 CVE 모니터링                                                              | ✅   |
| OWASP A10            | 스택 트레이스 사용자 반환 금지                                                   | ✅   |
| ASVS V1              | 위협 모델링·Secure by Design                                                     | ✅   |
| ASVS V4              | IDOR·CORS 검증                                                                   | ✅   |
| ASVS V7              | 구조화 로깅·민감정보 마스킹                                                      | ✅   |
| ASVS V13             | API Rate Limiting·토큰 관리                                                      | ✅   |
| ASVS V14             | Actuator 제한·환경 분리                                                          | ✅   |
| ASVS V17             | 의존성 스캔·SBOM                                                                 | ✅   |
| JWT                  | 모든 JWT 서명·클레임 검증 (alg allow-list, iss/aud)                              | ✅   |
| JWT                  | jti 포함, Revocation(블랙리스트) 지원                                            | ✅   |
| JWT                  | 웹: 쿠키(HttpOnly/Secure/SameSite), localStorage 금지                            | ✅   |
| RSA/키               | 개인키 HSM/Vault/KMS, 단일 목적 원칙                                             | ✅   |
| OAuth/OIDC           | Authorization Code + PKCE, Implicit/Password Grant 금지                          | ✅   |
| OAuth/OIDC           | Redirect URI allow-list, State/Nonce 필수                                        | ✅   |
