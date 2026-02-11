# 배포 가이드 (Step 20)

> RULE 1.1(비밀정보 외부 주입), PRD 7(운영 기준), RULE 5.3(긴급 비활성화) 기준.

## 1. 필요 런타임

| 구성요소  | 버전     | 비고                                 |
| --------- | -------- | ------------------------------------ |
| **Java**  | 21       | Spring Boot 4.x 요구. JRE 또는 JDK   |
| **MySQL** | 8.0      | 운영 DB. 개발은 H2 가능              |
| **Redis** | 7.x 권장 | Refresh Token·블랙리스트·(선택) 캐시 |

- 운영 환경은 **MySQL + Redis** 기준 설계(PRD 4.2).
- 로컬/개발: H2 + Redis 미사용(Step 5.1 Fallback) 가능.

---

## 2. 환경 변수 목록 (운영)

모든 비밀정보·운영 설정은 **환경 변수 또는 Secret Manager**로만 주입(RULE 1.1).
`application-prod.yml`에서 참조하는 변수만 정리.

### 2.1 필수 (비밀정보)

| 변수             | 설명                       | 예시                                            |
| ---------------- | -------------------------- | ----------------------------------------------- |
| `DB_URL`         | JDBC URL                   | `jdbc:mysql://host:3306/dbname?useSSL=true&...` |
| `DB_USERNAME`    | DB 사용자                  | `app_user`                                      |
| `DB_PASSWORD`    | DB 비밀번호                | (Secret Manager 권장)                           |
| `DB_DRIVER`      | (선택)                     | `com.mysql.cj.jdbc.Driver`                      |
| `REDIS_HOST`     | Redis 호스트               | `redis.example.com`                             |
| `REDIS_PORT`     | Redis 포트                 | `6379`                                          |
| `REDIS_PASSWORD` | Redis 비밀번호             | 비밀번호 없으면 비워둠                          |
| `JWT_ISSUER`     | JWT iss 클레임             | `https://api.example.com`                       |
| `JWT_AUDIENCE`   | JWT aud 클레임             | `spring-thymleaf-map-sns-mng`                   |
| `JWT_SECRET_KEY` | JWT 서명 키 (256비트 이상) | (Secret Manager 권장)                           |

### 2.2 선택·오버라이드

| 변수                          | 기본값(prod)                         | 설명                                |
| ----------------------------- | ------------------------------------ | ----------------------------------- |
| `SERVER_PORT`                 | 8080                                 | 서버 포트                           |
| `SPRING_PROFILES_ACTIVE`      | prod                                 | 프로파일                            |
| `CORS_ALLOWED_ORIGINS`        | (필수 설정)                          | 쉼표 구분 허용 오리진. `*` 금지     |
| `COOKIE_SECURE`               | true                                 | 쿠키 Secure 플래그                  |
| `JWT_ACCESS_TTL_MINUTES`      | 15                                   | Access Token 유효기간(분)           |
| `JWT_REFRESH_TTL_DAYS`        | 30                                   | Refresh Token 유효기간(일)          |
| `RATE_LIMIT_LOGIN_CAPACITY`   | 10                                   | 로그인 분당 제한                    |
| `RATE_LIMIT_SIGNUP_CAPACITY`  | 10                                   | 가입 분당 제한                      |
| `RATE_LIMIT_REFRESH_CAPACITY` | 20                                   | 토큰 갱신 5분당 제한                |
| `UPLOAD_BASE_PATH`            | /var/uploads                         | 파일 업로드 경로                    |
| `LOG_LEVEL_ROOT`              | INFO                                 | 루트 로그 레벨                      |
| `LOG_LEVEL_APP`               | INFO                                 | com.example 로그 레벨               |
| `LOG_FILE_PATH`               | /var/log/spring_thymleaf_map_sns_mng | 로그 파일 경로                      |
| `DB_INIT_FAIL_TIMEOUT`        | -1                                   | DB 연결 실패 시 기동 유지(Step 5.1) |
| `JPA_DDL_AUTO`                | validate                             | 운영 시 validate 권장               |

### 2.3 지도·기타 (선택)

| 변수                   | 설명                           |
| ---------------------- | ------------------------------ |
| `MAP_PROVIDER`         | none / google / kakao / naver  |
| `MAP_GOOGLE_API_KEY`   | Google Maps API 키             |
| `MAP_KAKAO_API_KEY`    | 카카오 REST API 키             |
| `MAP_KAKAO_JS_APP_KEY` | 카카오 JavaScript 키 (웹 지도) |

---

## 3. 기동 방법

### 3.1 JAR 직접 실행

```bash
# 빌드
./gradlew bootJar

# 실행 (환경 변수는 OS/시스템드에서 설정)
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://...
export DB_USERNAME=...
export DB_PASSWORD=...
# ... (위 2장 변수 설정)

java -jar build/libs/app.jar
```

### 3.2 Docker Compose (infra)

- **인프라 + 백엔드**: [INFRA.md](./INFRA.md) 참고.
- `infra/docker-compose.yml`에서 `app-backend`는 `DB_*`, `REDIS_*`, `JWT_*`, `CORS_ALLOWED_ORIGINS` 등을 환경 변수로 주입.
- 운영 시 `infra/.env`에 실제 Secret 적용 후 `docker compose --profile backend up -d`.

---

## 4. 헬스체크

| 항목                | URL                    | 설명                                                     |
| ------------------- | ---------------------- | -------------------------------------------------------- |
| **Actuator Health** | `GET /actuator/health` | DB·Redis 등 상태(노출 제한: prod에서 show-details=never) |
| **Actuator Info**   | `GET /actuator/info`   | 앱 정보                                                  |
| **기타 Actuator**   | `/actuator/**`         | prod에서는 health, info만 노출. 나머지 deny              |

- 로드밸런서·오케스트레이션에서 **헬스체크** 시 `GET /actuator/health` 사용 권장.
- 인증 없이 호출 가능(Step 7 permitAll).

---

## 5. 로깅 (RULE 1.4.3)

`application-prod.yml`에 반영된 항목:

- **로그 레벨**: root·com.example INFO, repository WARN. 환경 변수로 오버라이드 가능.
- **파일**: `LOG_FILE_PATH` (기본 `/var/log/spring_thymleaf_map_sns_mng/app.log`).
- **롤링**: max-file-size 100MB, max-history 90일, gz 압축, total-size-cap 10GB.
- **패턴**: 콘솔은 색상 최소화(ELK/Datadog 수집 고려).

---

## 6. 긴급 비활성화 (RULE 5.3)

### 6.1 현재 적용 여부

- **Feature Toggle / Kill Switch**: 코드 내 전역 플래그로 기능 차단하는 구성은 **미적용**.
- **대안 적용**:
  - **Rate Limiting(Step 18)**: 로그인·가입·토큰 갱신 등에 IP 기준 제한. 과도 호출 시 429로 차단.
  - **DB·Redis Fallback(Step 5.1)**: DB/Redis 연결 실패 시 해당 기능만 비활성화, 서버는 기동 유지.

### 6.2 운영 시 권장

- 장애 시 **배포 없이** 기능을 끄려면:
  - **환경 변수**로 “기능 on/off” 플래그를 두고, 앱에서 읽어 분기하도록 확장 가능.
  - 또는 **Rate Limit 수치를 극단적으로 낮춰** 사실상 해당 API만 차단하는 방법을 응급용으로 사용 가능.
- Kill Switch 도입 시 **적용 대상·운영 절차·권한**을 별도 문서로 정리할 것.

---

## 7. 참고 문서

| 문서                                                       | 내용                                     |
| ---------------------------------------------------------- | ---------------------------------------- |
| [INFRA.md](./INFRA.md)                                     | Docker Compose, MySQL/Redis/Backend 구성 |
| [SERVER_STARTUP_GUIDE.md](./SERVER_STARTUP_GUIDE.md)       | 로컬 서버 구동·프로파일                  |
| [AUTH_DESIGN.md](./AUTH_DESIGN.md)                         | JWT·인증 흐름                            |
| [API_SPEC.md](./API_SPEC.md)                               | REST API 명세                            |
| [FLUTTER_API_INTEGRATION.md](./FLUTTER_API_INTEGRATION.md) | Flutter 연동·인증·지도 예시              |

---

**최종 업데이트**: 2026-02-11 (Step 20)
