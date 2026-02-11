# 서버 구동 가이드

본 문서는 Spring Boot 프로젝트의 **프로파일별 데이터베이스 설정**과 **서버 구동 방법**을 설명하는 **공통 가이드**입니다. 프로젝트의 `application-*.yml` 구조에 맞게 적용하세요.

---

## 목차

1. [프로파일별 데이터베이스 설정](#1-프로파일별-데이터베이스-설정)
2. [DB 접속 여부와 상관없이 서버 구동하는 방법](#2-db-접속-여부와-상관없이-서버-구동하는-방법)
3. [서버 접속 로깅](#3-서버-접속-로깅)
4. [참고 문서](#4-참고-문서)

---

## 1. 프로파일별 데이터베이스 설정

### 개요

프로젝트는 개발 환경과 운영 환경을 분리하여 관리합니다:

- **개발 환경 (dev)**: H2 인메모리, Redis 미사용, 세션 none (환경 변수 없이 실행 가능)
- **운영 환경 (prod)**: MySQL + Redis (환경 변수 주입)

### 1.1 프로파일 구조

프로젝트는 다음 설정 파일로 구성됩니다:

- `application.yml`: 공통 설정, 기본 프로파일
- `application-dev.yml`: 개발 환경 (H2, Redis 미사용)
- `application-prod.yml`: 운영 환경 (MySQL, Redis - 환경 변수 주입)
- `application-local.yml` (선택, .gitignore): 프로젝트 루트에 두면 오버라이드 적용 (예: 카카오맵 키)

### 1.2 개발 환경 (dev)

#### 기본 프로파일

`application.yml`에서 기본 프로파일이 설정되어 있습니다:

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

환경 변수 `SPRING_PROFILES_ACTIVE`가 없으면 `dev`가 사용됩니다.

#### H2 인메모리 DB 사용 시

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:appdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: update
```

**H2 데이터베이스 특징:**

- 인메모리 데이터베이스로 별도 설치 불필요
- 서버 재시작 시 데이터 초기화
- 개발 및 테스트에 최적화
- `ddl-auto: update`로 엔티티 변경 시 자동 스키마 업데이트

#### 환경 변수 기반 사용 시

많은 프로젝트에서 dev/local도 환경 변수로 DB/Redis를 설정합니다:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:h2:mem:localdb;MODE=MySQL;DB_CLOSE_DELAY=-1}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:}
    driver-class-name: ${DB_DRIVER:org.h2.Driver}
```

이 경우 기본값으로 H2를 사용하고, `DB_URL` 등을 설정하면 MySQL 등으로 전환할 수 있습니다.

#### 개발 환경 실행 방법

```bash
# 기본 프로파일(dev)로 실행
./gradlew bootRun

# 명시적으로 프로파일 지정
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### 한글 로그 깨짐 방지 (Windows 터미널)

`build.gradle`의 `bootRun` 태스크에 UTF-8 JVM 인자가 설정되어 있는지 확인하세요. 터미널에서 한글이 깨지면:

- **PowerShell** (실행 후 한 번만):

  ```powershell
  [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
  chcp 65001
  ```

- **VS Code / Cursor 터미널**: UTF-8 사용 설정 권장

### 1.3 운영 환경 (prod) - MySQL + Redis

#### MySQL 설정

`application-prod.yml`에서 환경 변수로 MySQL 연결 정보를 주입합니다:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}

  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:validate}
```

**환경 변수 예시 (Docker / .env):**

| 변수          | 예시                                                                         | 설명           |
| ------------- | ---------------------------------------------------------------------------- | -------------- |
| `DB_URL`      | `jdbc:mysql://app-db:3306/app_db?useSSL=false&serverTimezone=Asia/Seoul&...` | JDBC URL       |
| `DB_USERNAME` | `app_user`                                                                   | MySQL 사용자   |
| `DB_PASSWORD` | (비밀번호)                                                                   | MySQL 비밀번호 |

#### MySQL 연결 URL 파라미터

| 파라미터                  | 값         | 설명                                  |
| ------------------------- | ---------- | ------------------------------------- |
| `useSSL`                  | false      | SSL 사용 안 함 (개발/내부망)          |
| `serverTimezone`          | Asia/Seoul | 서버 타임존 설정                      |
| `characterEncoding`       | UTF-8      | 문자 인코딩                           |
| `connectTimeout`          | 5000       | 연결 타임아웃 (ms)                    |
| `allowPublicKeyRetrieval` | true       | Public Key 인증 허용 (MySQL 8.x 필수) |

#### Redis 설정

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

#### 운영 환경 실행 방법

```bash
# prod 프로파일로 실행 (환경 변수 필수)
./gradlew bootRun --args='--spring.profiles.active=prod'

# JAR 실행 시
java -jar app.jar --spring.profiles.active=prod
```

Docker 환경에서는 [INFRA.md](./INFRA.md)를 참고하세요.

### 1.4 주의사항

1. **로컬/개발 환경 (H2):**
   - 서버 재시작 시 모든 데이터가 초기화됩니다
   - 개발 및 테스트 목적으로만 사용하세요
   - 운영 환경에서는 절대 사용하지 마세요

2. **운영 환경 (MySQL):**
   - MySQL 서버가 실행 중이어야 합니다
   - 데이터베이스 스키마가 미리 생성되어 있어야 합니다
   - `ddl-auto: validate` 또는 `none` 권장 (자동 스키마 생성 비활성화)

3. **Redis 연결 실패 시 제한 사항:**
   - 캐시 기능 사용 불가
   - 분산 락 사용 불가
   - Refresh Token / 세션 스토어 fallback 동작 (프로젝트 구현에 따름)

---

## 2. DB 접속 여부와 상관없이 서버 구동하는 방법

### 개요

개발 환경에서 MySQL/Redis가 실행되지 않은 상태에서도 Spring Boot 서버를 구동할 수 있도록 설정합니다.

**주의:** 이 기능은 개발/테스트 환경에서만 사용하세요. 운영 환경에서는 DB/Redis 연결이 필수입니다.

### 2.1 핵심 설정

#### DataSource (HikariCP) 설정

```yaml
spring:
  datasource:
    hikari:
      initialization-fail-timeout: -1 # DB 연결 실패해도 서버 시작
      connection-timeout: 5000 # 연결 타임아웃 5초
```

- `initialization-fail-timeout: -1`: HikariCP가 초기 연결 실패해도 애플리케이션 시작 허용
- `connection-timeout: 5000`: 연결 시도 타임아웃 5초

#### JPA/Hibernate 설정

```yaml
spring:
  jpa:
    defer-datasource-initialization: true
```

### 2.2 제한 사항

1. **DB 연결 실패 시:**
   - JPA Repository 호출 시 예외 발생
   - 트랜잭션 처리 불가
   - 데이터 조회/저장 불가

2. **Redis 연결 실패 시:**
   - 캐시 기능 사용 불가
   - 분산 락 사용 불가
   - TokenStore 등 fallback 전략 적용 시 제한적 동작

---

## 3. 서버 접속 로깅

### 개요

애플리케이션 시작 시 데이터베이스(H2 또는 MySQL)/Redis 연결 상태를 콘솔에 출력하여 인프라 연결 상태를 한눈에 확인합니다.

### 3.1 구현 패턴

`ApplicationRunner` 또는 `@Bean ApplicationRunner`를 사용하여 시작 직후 DB/Redis ping을 수행합니다.

**예시 (ConnectionHealthConfig):**

```java
@Configuration
@Profile("!test")
public class ConnectionHealthConfig {

    @Bean
    public ApplicationRunner dbConnectionHealthLogger(DataSource dataSource) {
        return args -> {
            try (var conn = dataSource.getConnection()) {
                boolean valid = conn.isValid(3);
                log.info(valid ? "DB 연결 성공" : "DB 연결 검증 실패");
            } catch (Exception e) {
                log.error("DB 연결 실패: {}", e.getMessage());
            }
        };
    }

    @Bean
    public ApplicationRunner redisConnectionHealthLogger(StringRedisTemplate redisTemplate) {
        return args -> {
            try {
                String pong = redisTemplate.getConnectionFactory().getConnection().ping();
                log.info("Redis 연결 성공: pong={}", pong);
            } catch (Exception e) {
                log.error("Redis 연결 실패: {}", e.getMessage());
            }
        };
    }
}
```

### 3.2 파일 위치

프로젝트 base package의 config 디렉터리에 위치합니다.

- 예: `src/main/java/com/example/{project}/config/ConnectionHealthConfig.java`

### 3.3 출력 예시

#### 연결 성공 시

```text
DB 연결 성공
Redis 연결 성공: pong=PONG
```

#### 연결 실패 시

```text
DB 연결 실패: Connection refused. DB 기반 API 호출 시 503 반환.
Redis 연결 실패: Connection refused. TokenStore NoOp fallback 적용.
```

---

## 4. 참고 문서

- [INFRA.md](./INFRA.md) - Docker 인프라 구축 (MySQL, Redis, Backend 포함)
- [DEVELOPMENT_ENVIRONMENT.md](./DEVELOPMENT_ENVIRONMENT.md) - 개발환경 세팅
- [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) - 문제 해결

---

**Last Updated:** 2026-02-06
