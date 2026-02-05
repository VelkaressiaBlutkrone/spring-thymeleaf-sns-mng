# 인증 설계 문서

> PRD 3.1(회원 시스템), 4.1.3(Redis 활용), RULE 1.2(인증·인가), **6.1~6.5(JWT·토큰 관리)** 기준.

## 1. 개요

| 항목 | 내용 |
|------|------|
| 인증 방식 | **JWT (Access Token + Refresh Token)** |
| Access Token | Bearer 헤더, 유효기간 **15분 이하** (RULE 6.1.4) |
| Refresh Token | Redis 저장, 웹: HttpOnly+Secure+SameSite=Strict 쿠키 (RULE 6.1.6, 6.5) |
| 역할 | ROLE_USER, ROLE_ADMIN |
| 확장 고려 | OAuth2 (Google/Kakao/Naver) |

---

## 2. JWT 토큰 규격 (RULE 6.1 준수)

### 2.1 Access Token

| 항목 | 값 | RULE |
|------|-----|------|
| 유효기간 | **15분 이하** | 6.1.4 |
| 알고리즘 | RS256 또는 ES256 (allow-list) | 6.1.2 |
| iss | 서버 식별자 (예: `https://api.example.com`) | 6.1.3 |
| aud | 리소스 서버 식별자 | 6.1.3 |
| jti | JWT ID (무효화용, 필수) | 6.1.7 |
| exp, nbf, iat | 모두 검증, Clock skew 최대 30초 | 6.1.4 |
| Payload | sub(userId), role 등 **민감정보 최소화** | 6.1.5 |

### 2.2 Refresh Token

| 항목 | 값 | RULE |
|------|-----|------|
| 저장소 | **Redis** | 6.5 |
| 유효기간 | 1일~30일 | 6.1.4 |
| Revocation | 로그아웃·탈취 시 즉시 무효화 | 6.1.7 |
| 웹 전달 | HttpOnly + Secure + SameSite=Strict 쿠키 | 6.1.6 |
| 모바일 | Secure Storage (flutter_secure_storage) | 6.1.6 |

---

## 3. 인증 흐름

### 3.1 로그인 흐름

```
[Client]                    [Backend]                    [Redis]

   | POST /api/auth/login        |                              |
   | (email, password)           |                              |
   |--------------------------->|                              |
   |                            | 비밀번호 검증 (BCrypt)        |
   |                            | Access Token 발급 (15분)     |
   |                            | Refresh Token 생성            |
   |                            |----------------------------->|
   |                            |   Refresh Token 저장 (jti)    |
   |                            |<-----------------------------|
   | 200 OK                     |                              |
   | Body: { accessToken }       |                              |
   | Set-Cookie: refreshToken   |  (HttpOnly, Secure, SameSite)|
   |<---------------------------|                              |
```

### 3.2 인증된 요청 흐름

```
[Client]                    [Backend]                    [Redis]

   | GET /api/me/pins             |                              |
   | Authorization: Bearer {AT}   |                              |
   |--------------------------->|                              |
   |                            | JWT 서명·클레임 검증 (iss,aud,exp) |
   |                            | jti 블랙리스트 조회 (선택)    |
   |                            |----------------------------->|
   |                            |<-----------------------------|
   |                            | 인가 검증 (SecurityContext)   |
   | 200 OK + 데이터             |                              |
   |<---------------------------|                              |
```

### 3.3 토큰 갱신(Refresh) 흐름

```
[Client]                    [Backend]                    [Redis]

   | POST /api/auth/refresh      |                              |
   | Cookie: refreshToken=xxx    |                              |
   |--------------------------->|                              |
   |                            | Refresh Token 검증            |
   |                            | Redis에서 jti 조회·유효성 확인|
   |                            |----------------------------->|
   |                            |<-----------------------------|
   |                            | 새 Access Token 발급          |
   | 200 OK                     |                              |
   | Body: { accessToken }       |                              |
   |<---------------------------|                              |
```

### 3.4 로그아웃 흐름

```
[Client]                    [Backend]                    [Redis]

   | POST /api/auth/logout       |                              |
   | Authorization: Bearer {AT} |                              |
   | Cookie: refreshToken=xxx   |                              |
   |--------------------------->|                              |
   |                            | Access Token jti 블랙리스트 등록 |
   |                            |----------------------------->|
   |                            | Refresh Token 삭제            |
   |                            |----------------------------->|
   | 200 OK                     |                              |
   | Set-Cookie: refreshToken=  |  (쿠키 삭제/만료)             |
   |<---------------------------|                              |
```

---

## 4. API 엔드포인트

### 4.1 인증 API

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/auth/login` | 로그인. Body: accessToken, Set-Cookie: refreshToken |
| POST | `/api/auth/refresh` | 토큰 갱신. Cookie: refreshToken → Body: accessToken |
| POST | `/api/auth/logout` | 로그아웃. jti 블랙리스트, Refresh Token 삭제 |
| GET | `/api/auth/me` | 현재 사용자. Authorization: Bearer {accessToken} |

### 4.2 비인증 허용 경로

| 경로 | 용도 |
|------|------|
| `/api/members` (POST) | 회원가입 |
| `/api/auth/login` (POST) | 로그인 |
| `/api/auth/refresh` (POST) | 토큰 갱신 |
| `/api/posts` (GET) | 게시글 목록·상세 |
| `/api/image-posts` (GET) | 이미지 게시글 목록·상세 |
| `/api/pins/nearby` (GET) | 반경 내 Pin 조회 |
| `/actuator/health`, `/actuator/info` | 헬스체크 |
| `/swagger-ui/**`, `/v3/api-docs/**` | API 문서 |

### 4.3 인증 필수 경로

| 경로 | 용도 |
|------|------|
| `/api/auth/logout` (POST) | 로그아웃 |
| `/api/auth/me` (GET) | 현재 사용자 조회 |
| `/api/posts` (POST), `/api/posts/{id}` (PUT, DELETE) | 게시글 작성·수정·삭제 |
| `/api/image-posts` (POST), `/api/image-posts/{id}` (PUT, DELETE) | 이미지 게시글 CRUD |
| `/api/pins` (전체), `/api/pins/{id}` (PUT, DELETE) | Pin CRUD |
| `/api/me/**` | 마이페이지 전용 |

### 4.4 관리자(ROLE_ADMIN) 전용 경로

| 경로 | 용도 |
|------|------|
| `/api/admin/**` | 회원 관리, 게시물 관리, 통계 |

---

## 5. 역할(Role) 정의

| 역할 | 설명 | 권한 |
|------|------|------|
| ROLE_USER | 일반 회원 | 게시글/Pin 작성·수정·삭제, 마이페이지 |
| ROLE_ADMIN | 관리자 | 전체 회원·게시물 관리, 통계, 공지 등록 |

---

## 6. Redis 활용 (RULE 6.1.7, 6.5)

| 용도 | 키 패턴 | TTL | 설명 |
|------|---------|-----|------|
| Refresh Token | `refresh:{jti}` | 7~30일 | jti → userId, role 등 |
| Access Token 블랙리스트 | `blacklist:{jti}` | Access Token 만료시간 | 로그아웃 시 jti 등록 |
| (선택) 위치 캐시 | `location:*` | 별도 정책 | 반경 조회 캐시 |

---

## 7. 보안 고려사항 (RULE 준수)

| 항목 | RULE | 적용 |
|------|------|------|
| Access Token 유효기간 | 6.1.4 | 15분 이하 |
| Refresh Token | 6.5 | Redis 저장, HttpOnly 쿠키(웹) |
| jti 필수 | 6.1.7 | Revocation 지원 |
| iss, aud 검증 | 6.1.3 | 모든 JWT 검증 시 |
| alg allow-list | 6.1.2 | RS256/ES256만 허용 |
| 민감정보 Payload 금지 | 6.1.5 | 이메일 등 별도 API |
| 비밀번호 | 1.5.6 | BCrypt 또는 Argon2id |
| 인증 실패 | 1.2.2 | 401 Unauthorized |
| 권한 부족 | 1.2.2 | 403 Forbidden |
| 로깅 | 1.4.2 | 인증 실패·권한 부족 시도 로깅 |

---

## 8. OAuth2 확장 포인트

향후 SNS 로그인 확장 시:

- OAuth2 성공 시 **JWT 발급** (세션 대신)
- Authorization Code Flow + PKCE (RULE 6.4.2)
- Refresh Token은 동일하게 Redis 저장

---

## 9. 참조

- **API 명세**: `doc/API_SPEC.md`
- **ERD**: `doc/ERD.md`
- **보안 규칙**: `doc/RULE.md` **6.1~6.5**, 1.2, 1.5.6

---

> **문서 버전**: 1.1.0
> **기준**: PRD 3.1, 4.1.3, RULE 6.1~6.5
> **최종 업데이트**: 2026-02-05
