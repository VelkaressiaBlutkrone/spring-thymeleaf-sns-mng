# 부록 A: ASVS 5.0 챕터별 대응 방안 (문제 → 대책)

> **참고**: ASVS 5.0은 공식 릴리스 시점에 따라 **드래프트/예상** 상태일 수 있다. 문서 참조 시 공식 OWASP ASVS 버전을 확인하고, 드래프트 기반임을 인지한 상태에서 적용한다. 원본: [RULE.md](../RULE.md) 부록 A.

ASVS 5.0(Application Security Verification Standard 5.0) 17개 챕터 기반 **문제 → 대책** 요구사항.

| 챕터    | 챕터 이름                                | 문제(위험)                                                    | 대책(요구사항)                                                                | RULE 매핑      |
| ------- | ---------------------------------------- | ------------------------------------------------------------- | ----------------------------------------------------------------------------- | -------------- |
| **V1**  | Architecture, Design and Threat Modeling | SDLC 보안 미통합, 위협 모델링 부재, 비안전 설계               | SDLC에 보안 단계 통합, 위협 모델링(STRIDE 등), Secure by Design               | 2.1, 3.1       |
| **V2**  | Authentication                           | MFA 미적용, 크리덴셜 평문 저장·전송, OAuth/JWT 불안전 구현    | MFA, BCrypt/Argon2id, OAuth 2.1/OIDC/JWT 안전 구현                            | 1.5.6, 6.1~6.3 |
| **V3**  | Session Management                       | 세션 ID 예측 가능, 로그아웃·타임아웃 미흡, CSRF 취약          | 세션 ID CSPRNG, 로그아웃 시 무효화·타임아웃, SameSite=Strict·CSRF 토큰        | 1.5.6, 6.3     |
| **V4**  | Access Control                           | RBAC/ABAC 미적용, IDOR, CORS 과도 허용                        | RBAC/ABAC, 최소 권한, 리소스 소유권 검증, CORS 최소화                         | 1.2            |
| **V5**  | Validation, Sanitization and Encoding    | 입력 미검증, XSS·인젝션                                       | @Valid·화이트리스트, 출력 인코딩, DOMPurify·파라미터화 쿼리                   | 1.3            |
| **V6**  | Stored Cryptography                      | 약한 알고리즘, 키 관리 미흡, 비밀번호 취약 해싱               | AES-256-GCM, KMS/Vault, Argon2id/PBKDF2                                       | 1.5.2          |
| **V7**  | Error Handling and Logging               | 스택 트레이스 노출, 로그 민감정보, 모니터링 부재              | 스택 트레이스 사용자 반환 금지, 구조화 로깅, 민감정보 마스킹, 모니터링·알림   | 1.4, 2.2       |
| **V8**  | Data Protection                          | 과다 저장, 마스킹 미적용, 백업 암호화 미흡                    | 데이터 분류·최소 저장, PII 마스킹, 백업 암호화                                | 1.1            |
| **V9**  | Communications                           | TLS 미적용·약함, HSTS 미설정, 인증서 검증 생략                | TLS 1.3 강제, HSTS, Certificate Pinning(모바일), HTTP/2·3                     | 1.5.1          |
| **V10** | Malicious Code / Code Tampering          | 무결성 미검증, 서명 부재, 런타임 변조                         | 아티팩트 서명·검증, RASP/런타임 보호                                          | 1.5.3, 1.7     |
| **V11** | Business Logic                           | 레이스 컨디션, 무제한 쿠폰·포인트 악용                        | 동시성 제어(분산 락), 비즈니스 규칙 검증, 서비스 계층 검증                    | 2.1, 2.3       |
| **V12** | Files and Resources                      | 파일 업로드 검증 누락, 경로 traversal, 안전 저장 미흡         | 파일 타입·크기·콘텐츠 검증, 경로 traversal 방지, 외부 저장 경로 분리          | 1.3            |
| **V13** | API Security                             | REST/GraphQL 인증·검증 부재, Rate Limiting 없음               | API 인증·권한, Rate Limiting, API 키·토큰 안전 관리                           | 2.1, 1.5.4     |
| **V14** | Configuration                            | Actuator 노출, 불필요 기능 활성화, 환경 혼재                  | Actuator 제한·인증, 불필요 기능 비활성화, 환경별 설정 분리                    | 1.6, 5.1       |
| **V15** | WebSockets / Real-time                   | WebSocket 인증·권한 미흡, 메시지 미검증, DoS                  | 핸드셰이크 인증·권한, 메시지 검증, DoS 방어                                   | 1.2, 1.5.4     |
| **V16** | Mobile                                   | 루트/탈옥 탐지 미흡, Secure Storage 미사용, Cert Pinning 없음 | 루트·탈옥 탐지(필요 시), flutter_secure_storage/Keychain, Certificate Pinning | 7.3            |
| **V17** | Software Supply Chain & Dependencies     | 의존성 CVE, SBOM 부재, 공급망 공격                            | 의존성 스캔, SBOM 생성, 서명 검증                                             | 1.7            |

상세 대책 요약은 원본 RULE.md 부록 A 또는 [01-security.md](01-security.md), [02-function.md](02-function.md) 등 해당 챕터 규칙 문서 참조.
