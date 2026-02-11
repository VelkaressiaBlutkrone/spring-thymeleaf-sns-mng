# 데이터베이스 관련 문제

## H2 콘솔 접속 (개발 환경)

개발/로컬 환경에서 H2 인메모리 데이터베이스를 사용하는 경우 H2 Console로 접속할 수 있습니다.

### 접속 정보 (dev 환경)

| 항목         | 값                                               |
| ------------ | ------------------------------------------------ |
| 브라우저 URL | `http://localhost:8080/h2-console`               |
| JDBC URL     | `jdbc:h2:mem:devdb;MODE=MySQL;DB_CLOSE_DELAY=-1` |
| Driver Class | `org.h2.Driver`                                  |
| Username     | `sa`                                             |
| Password     | (비워두기)                                       |

> **중요**: JDBC URL은 application-dev.yml과 **동일**해야 합니다. URL이 다르면 별도 인메모리 DB가 생성되어 기동 중인 앱의 데이터와 연결되지 않습니다.

### 설정 예시

`application-dev.yml`:

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

### 주의사항

- H2 콘솔은 **개발 환경에서만** 활성화하세요.
- `web-allow-others: true`는 보안상 주의가 필요합니다.
- 인메모리 DB는 서버 재시작 시 데이터가 초기화됩니다.

---

## 문제: 데이터베이스 연결 실패

**에러 메시지:**

```text
Communications link failure
```

**해결 방법:**

### 1. 설정 파일 확인

`application-prod.yml` 또는 환경 변수에서 DB URL, 사용자, 비밀번호 확인:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 2. 데이터베이스 서버 실행 확인

MySQL이 실행 중인지 확인:

```bash
# Windows
net start MySQL80

# Linux/Mac
sudo systemctl start mysql

# Docker
docker compose ps app-db
```

### 3. 데이터베이스 생성

```sql
CREATE DATABASE IF NOT EXISTS app_db;
```

### 4. Docker 사용 시

[INFRA.md](../INFRA.md) 참고하여 `docker compose up -d app-db` 실행 후 Backend 기동.

---

## 포트 충돌 문제

### 문제: 포트가 이미 사용 중

**에러 메시지:**

```text
Port 8080 is already in use
```

**해결 방법:**

### 1. 사용 중인 포트 확인

Windows:

```powershell
netstat -ano | findstr :8080
```

Linux/Mac:

```bash
lsof -i :8080
```

### 2. 프로세스 종료

Windows:

```powershell
taskkill /PID <PID> /F
```

Linux/Mac:

```bash
kill -9 <PID>
```

### 3. 포트 변경

`application.yml` 또는 환경 변수:

```properties
server.port=8081
```

또는 `SERVER_PORT=8081` (환경 변수 사용 시).
