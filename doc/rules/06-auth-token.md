# 6. 인증/토큰 관리 규칙 [MUST]

> 원본: [RULE.md](../RULE.md) 6장.

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

| CIA 요소                     | 핵심 Rule                                                                                                         | 이유 / 위협 방어       |
| ---------------------------- | ----------------------------------------------------------------------------------------------------------------- | ---------------------- |
| **Confidentiality (기밀성)** | HTTPS 강제 (HSTS Preload 포함), 민감 데이터는 JWT에 넣지 말고 별도 암호화 전송, PII 최소화 + Pseudonymous ID 사용 | 데이터 유출, MITM, XSS |
| **Integrity (무결성)**       | 모든 JWT 서명 검증, RSA/ECDSA 등 강력 알고리즘, 메시지 무결성 검증 (MAC, 서명)                                    | 위·변조 공격           |
| **Availability (가용성)**    | Rate Limiting + DDoS 방어, 토큰 블랙리스트가 성능 저하시키지 않도록 설계 (Redis 등), 키 로테이션 시 무중단 처리   | DoS, 서비스 거부       |

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
