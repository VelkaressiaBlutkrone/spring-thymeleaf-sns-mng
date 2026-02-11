# Application 프로파일 및 카카오맵 관련 문제

본 문서는 Spring Boot **application 프로파일**(dev/prod) 설정과 **카카오맵 API 키·CSP 인라인 스크립트** 관련 문제를 정리합니다.

---

## 1. Application 프로파일

### 1.1 프로파일 구조

| 구분 | 파일 | 비고 |
|------|------|------|
| 루트 | `application.yml` | `spring.profiles.active`, 공통 설정 |
| 프로파일 | `application-dev.yml` | 개발: H2, Redis 미사용 |
| 프로파일 | `application-prod.yml` | 운영: MySQL, Redis |
| import | `spring.config.import=optional:file:./application-local.yml` | 프로젝트 루트의 `application-local.yml` 오버라이드 (선택) |

- **dev** 기동 시: H2 인메모리, Redis 미사용, 세션 none
- **prod** 기동 시: MySQL·Redis 환경 변수 필수

---

### 1.2 `DB_URL` / `jdbcUrl, ${DB_URL}` — 환경 변수 미설정

**에러 메시지:**

```text
Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, ${DB_URL}
Schema validation: missing table [image_posts]
```

**원인:**

- `application-dev.yml`에서 `spring.datasource.url: ${DB_URL}` 로 두었을 때, 환경 변수 `DB_URL`이 없으면 플레이스홀더가 **그대로** 전달됨.
- MySQL 드라이버가 `"${DB_URL}"` 문자열을 jdbcUrl로 받아 에러 발생.
- 또는 H2를 쓰더라도 `ddl-auto: validate` 이면 빈 DB에서 테이블이 없어 스키마 검증 실패.

**해결 방법:**

1. **개발에서 환경 변수 없이 실행하려면**
   - `application-dev.yml` 에 이미 **기본값**이 있음:
     - `url: ${DB_URL:jdbc:h2:mem:devdb;MODE=MySQL;DB_CLOSE_DELAY=-1}`
     - `driver-class-name: ${DB_DRIVER:org.h2.Driver}`
     - `username: ${DB_USERNAME:sa}`, `password: ${DB_PASSWORD:}`
   - dev 프로파일은 Redis 미사용·세션 none으로 기동 가능.

2. **MySQL 사용 시**
   - 환경 변수 설정: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER=com.mysql.cj.jdbc.Driver`
   - 운영 DB에서는 `JPA_DDL_AUTO=validate` 로 설정 권장.

3. **application-local.yml (선택)**
   - `.gitignore` 대상인 `application-local.yml` 에 DB/JWT 오버라이드를 두면, 개발 시 추가 설정 가능.
   - 예시는 `application-local.example.yml` 참고.

---

## 2. 카카오맵 API 키

### 2.0 실행 구조와 키 적용 경로 (API Key 반영 안 될 때 확인)

**키가 전달되는 경로:**

1. **설정 로드**: `application.yml` → `spring.profiles.active` 기본값이 `dev` 이므로 dev 프로파일 로드.
2. **키 값**: `application-local.yml`(optional import)의 `app.map.kakao-js-app-key` 가 dev의 `${MAP_KAKAO_JS_APP_KEY:}` 보다 나중에 적용되어 사용됨.
3. **해석**: `KakaoMapKeyResolver.resolve()` → `MapProperties.kakaoJsAppKey()` → 없으면 환경 변수 `MAP_KAKAO_JS_APP_KEY` → 없으면 JVM `-DMAP_KAKAO_JS_APP_KEY=...`.
4. **뷰**: `HomeController`(/) 및 `MapViewController`가 `kakaoJsAppKey`, `kakaoMapScriptUrl`을 모델에 넣고, `index.html` 등에서 `window.MAP_KAKAO_APP_KEY`와 스크립트 URL로 주입.

**키가 비어 있을 때 확인할 것 (순서대로):**

| 순서 | 확인 항목 | 조치 |
|------|-----------|------|
| 1 | 기동 로그에 `카카오맵 API 키 미설정` WARN 여부 | 나오면 키가 비어 있는 상태로 기동된 것. 아래 2~4 확인. |
| 2 | `application.yml` 의 `spring.profiles.active` 기본값 | 기본이 `dev` 이어야 함. |
| 3 | `application-local.yml` 존재 및 `app.map.kakao-js-app-key` 값 | 없으면 예시는 `application-local.example.yml` 참고해 프로젝트 루트에 생성. |
| 4 | IDE Run Configuration에서 Active profiles | 비워두면 기본값(dev) 적용. |
| 5 | 환경 변수 / VM 옵션 | 환경 변수 `MAP_KAKAO_JS_APP_KEY` 또는 VM 옵션 `-DMAP_KAKAO_JS_APP_KEY=발급받은키` 로 우회 가능. |
| 6 | 카카오 개발자 콘솔 | JavaScript 키 발급 후 **도메인**에 `http://localhost:8080` 등록. |

---

### 2.1 지도가 안 나오고 "카카오맵 API 키가 필요합니다"만 표시

**원인:**

- `app.map.kakao-js-app-key` 가 비어 있음.
- 환경 변수 `MAP_KAKAO_JS_APP_KEY` 도 없고, `application-local.yml` 의 키가 로드되지 않은 경우.

**해결 방법:**

1. **application-local.yml 사용 (권장)**
   - `application-local.example.yml` 을 복사해 `application-local.yml` 로 저장 (프로젝트 루트):
     ```yaml
     app:
       map:
         kakao-js-app-key: "발급받은_JavaScript_키"
     ```
   - `spring.config.import=optional:file:./application-local.yml` 이 있으면 **프로젝트 루트**의 동일 파일이 자동 로드됨.

2. **환경 변수 또는 VM 옵션**
   - 환경 변수: `MAP_KAKAO_JS_APP_KEY=발급받은_JavaScript_키`
   - VM 옵션(IDE): `-DMAP_KAKAO_JS_APP_KEY=발급받은_JavaScript_키`
   - `KakaoMapKeyResolver` 는 설정 파일 → 환경 변수 → 시스템 프로퍼티 순으로 사용함.

3. **카카오 개발자 콘솔**
   - JavaScript 키 발급 후, **도메인**에 `http://localhost:8080` 등 로컬 주소 등록 필요.

---

### 2.2 Content-Security-Policy로 인라인 스크립트 차단

**에러 메시지 (브라우저 콘솔):**

```text
Executing inline script violates the following Content Security Policy directive
'script-src 'self' https://dapi.kakao.com'. Either the 'unsafe-inline' keyword,
a hash ('sha256-...'), or a nonce ('nonce-...') is required to enable inline execution.
```

**원인:**

- Security 설정에서 CSP `script-src`에 `'self'`와 `https://dapi.kakao.com` 만 허용되어 있음.
- 카카오맵 키 등을 넣는 **인라인 스크립트**는 기본적으로 차단됨.
- `unsafe-inline` 을 넣지 않았기 때문에 nonce 또는 hash 없이는 인라인 실행 불가.

**해결 방법 (구현됨):**

- **nonce** 방식으로 인라인 스크립트만 허용하도록 되어 있음.
  1. **CspNonceFilter**: 요청마다 nonce 생성 → `request.setAttribute("cspNonce", nonce)` 및 응답 래퍼로 CSP 헤더의 `{nonce}` 치환.
  2. **SecurityConfig**: `script-src` 에 `'nonce-{nonce}'` 추가, `CspNonceFilter` 를 `HeaderWriterFilter` 앞에 등록.
  3. **CspNonceAdvice**: 모든 뷰에 `cspNonce` 모델 속성 추가.
  4. **템플릿**: 인라인/외부 스크립트용 `<script>` 에 `th:attr="nonce=${cspNonce}"` 추가 (index, post-detail, image-post-detail, post-create, image-post-create 등).

- 따라서 **별도 조치 없이** 인라인 스크립트는 nonce로만 실행되며, CSP는 유지됨.
- 문제가 계속되면 해당 페이지의 `<script>` 에 `nonce="${cspNonce}"`(또는 `th:attr="nonce=${cspNonce}"`) 가 빠진 곳이 없는지 확인.

---

## 3. 요약 표

| 현상 | 확인할 곳 | 조치 |
|------|------------|------|
| jdbcUrl `${DB_URL}` 그대로 전달 | 환경 변수 DB_URL | application-dev.yml 기본값(H2) 사용 또는 env 설정 |
| missing table [image_posts] | JPA ddl-auto | dev 기본값 create-drop 유지 |
| 지도 안 나옴 / API 키 필요 메시지 | app.map.kakao-js-app-key | application-local.yml 또는 MAP_KAKAO_JS_APP_KEY 설정, config.import 확인 |
| CSP로 인라인 스크립트 차단 | script-src, nonce | CspNonceFilter·CspNonceAdvice·템플릿 nonce 속성 적용 여부 확인 |

---

## 참고 문서

- [MAP_API.md](../MAP_API.md) - 지도 API 명세
- [SERVER_STARTUP_GUIDE.md](../SERVER_STARTUP_GUIDE.md) - 서버 구동 및 프로파일
- [application-local.example.yml](../../src/main/resources/application-local.example.yml) - 로컬 오버라이드 예시
