# Docker 인프라 구축 가이드

본 문서는 **MySQL, Redis를 포함한** 전체 Docker 인프라를 구축하고, 백엔드·프론트엔드·모바일(Flutter Web) 애플리케이션을 Docker로 실행하기 위한 **공통 설정 가이드**입니다. 프로젝트에 맞게 서비스명·환경변수·포트를 치환하여 사용할 수 있습니다.

---

## 목차

1. [개요](#1-개요)
2. [서비스 구성](#2-서비스-구성)
3. [사전 요구사항](#3-사전-요구사항)
4. [환경 변수 설정](#4-환경-변수-설정)
5. [실행 방법](#5-실행-방법)
6. [프로파일별 사용법](#6-프로파일별-사용법)
7. [포트 및 접속 정보](#7-포트-및-접속-정보)
8. [빌드 및 디버깅](#8-빌드-및-디버깅)
9. [문제 해결](#9-문제-해결)
10. [참고 문서](#10-참고-문서)

---

## 1. 개요

### 1.1 Docker Compose 서비스 구성

| 구분           | 서비스       | 프로파일                       | 설명                   |
| -------------- | ------------ | ------------------------------ | ---------------------- |
| **인프라**     | app-db       | (기본)                         | MySQL 8.0              |
|                | app-redis    | (기본)                         | Redis 7.x              |
| **백엔드**     | app-backend  | backend, frontend, mobile, app | Spring Boot (Java 21)  |
| **프론트엔드** | app-frontend | frontend, app                  | React + Vite (Node 20) |
| **모바일**     | app-mobile   | mobile, app                    | Flutter Web            |
| **선택**       | app-nginx    | with-nginx                     | Nginx 리버스 프록시    |

> **프로젝트 맞춤화**: `app-` 접두사를 프로젝트명(예: `myproject-db`, `myproject-backend`)으로 변경하여 사용하세요.

### 1.2 디렉터리 구조

```text
infra/
├── docker-compose.yml          # 메인 compose (MySQL, Redis, Backend, Frontend, Mobile)
├── .env                        # 환경 변수 (cp .env.example .env)
├── .env.example
├── docker/
│   ├── backend/
│   │   └── Dockerfile          # Spring Boot 빌드
│   ├── frontend/
│   │   ├── Dockerfile          # React + Vite 빌드
│   │   └── nginx.conf          # 프론트엔드 nginx 설정
│   └── mobile/
│       ├── Dockerfile          # Flutter Web 빌드
│       └── nginx.conf          # 모바일 nginx 설정
├── mysql/                      # MySQL 초기화 스크립트 (선택)
├── redis/                      # Redis 설정 (선택)
└── nginx/                      # Nginx 설정 (with-nginx 프로파일)
```

---

## 2. 서비스 구성

### 2.1 MySQL (app-db)

- **이미지**: MySQL 8.0
- **역할**: 관계형 DB 저장소
- **헬스체크**: `mysqladmin ping` (healthy 후 Backend 시작 가능)
- **볼륨**: 데이터 영속성용 volume 마운트
- **환경 변수**: `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`
- **초기화**: `mysql/init/` 디렉터리에 `.sql` 파일 두면 최초 기동 시 자동 실행

### 2.2 Redis (app-redis)

- **이미지**: Redis 7.x (Alpine)
- **역할**: 세션·캐시·토큰 스토어 등
- **헬스체크**: `redis-cli ping`
- **볼륨**: 데이터 영속성용 volume 마운트 (선택)
- **환경 변수**: `REDIS_PASSWORD` (선택, 비밀번호 설정 시)

### 2.3 Backend (app-backend)

- **이미지**: Eclipse Temurin 21 JRE (Alpine)
- **빌드**: Gradle `bootJar`
- **프로파일**: `prod` (MySQL, Redis 연결)
- **의존성**: app-db, app-redis (healthcheck 완료 후 시작)
- **환경 변수**:
  - `DB_URL`: `jdbc:mysql://app-db:3306/{DB_NAME}...`
  - `DB_USERNAME`: MySQL 사용자
  - `DB_PASSWORD`: MySQL 비밀번호
  - `REDIS_HOST`: `app-redis`
  - `REDIS_PORT`: `6379`
  - `SPRING_PROFILES_ACTIVE`: `prod`

### 2.4 Frontend (app-frontend)

- **이미지**: Node 20 (빌드) → Nginx Alpine (런타임)
- **빌드**: `npm ci` → `npm run build` (`VITE_API_BASE_URL=/api`)
- **의존성**: app-backend
- **API 프록시**: nginx가 `/api`, `/ws`, `/sse`, `/admin` → app-backend:8080

### 2.5 Mobile (app-mobile)

- **이미지**: Flutter SDK (빌드) → Nginx Alpine (런타임)
- **빌드**: `flutter build web` (상대 경로 `/api` 사용 시)
- **의존성**: app-backend
- **API 프록시**: nginx가 `/api`, `/ws`, `/sse` → app-backend:8080

---

## 3. 사전 요구사항

- **Docker Desktop** 4.0 이상 (또는 Docker Engine + Docker Compose V2)
- **디스크 여유 공간**: 약 3GB 이상 (이미지 + 빌드 캐시)
- **메모리**: 최소 4GB 권장

### 확인

```bash
docker --version
docker compose version
```

---

## 4. 환경 변수 설정

### 4.1 .env 파일 생성

```bash
cd infra
cp .env.example .env
# .env 파일을 편집하여 프로젝트에 맞게 수정
```

### 4.2 MySQL 관련 (.env)

| 변수                  | 기본값       | 설명                |
| --------------------- | ------------ | ------------------- |
| `MYSQL_ROOT_PASSWORD` | rootpassword | MySQL root 비밀번호 |
| `MYSQL_DATABASE`      | app_db       | DB 이름             |
| `MYSQL_USER`          | app_user     | DB 사용자           |
| `MYSQL_PASSWORD`      | (변경 권장)  | DB 비밀번호         |
| `MYSQL_PORT`          | 3306         | MySQL 포트          |

### 4.3 Redis 관련 (.env)

| 변수             | 기본값                   | 설명           |
| ---------------- | ------------------------ | -------------- |
| `REDIS_PORT`     | 6379                     | Redis 포트     |
| `REDIS_PASSWORD` | (비워두면 비밀번호 없음) | Redis 비밀번호 |

### 4.4 앱 포트 (.env, 선택)

| 변수            | 기본값 | 설명               |
| --------------- | ------ | ------------------ |
| `BACKEND_PORT`  | 8080   | Backend 노출 포트  |
| `FRONTEND_PORT` | 5173   | Frontend 노출 포트 |
| `MOBILE_PORT`   | 5174   | Mobile 노출 포트   |

### 4.5 Backend 연결용 환경 변수 (Backend 컨테이너)

Backend의 `application-prod.yml` 또는 환경 변수와 맞춰 설정:

| 변수             | 예시                                               | 설명           |
| ---------------- | -------------------------------------------------- | -------------- |
| `DB_URL`         | `jdbc:mysql://app-db:3306/app_db?useSSL=false&...` | JDBC URL       |
| `DB_USERNAME`    | `app_user`                                         | MySQL 사용자   |
| `DB_PASSWORD`    | `.env`의 `MYSQL_PASSWORD`와 동일                   | MySQL 비밀번호 |
| `REDIS_HOST`     | `app-redis`                                        | Redis 호스트   |
| `REDIS_PORT`     | `6379`                                             | Redis 포트     |
| `JWT_SECRET_KEY` | (운영 시 반드시 변경)                              | JWT 서명 키    |
| `JWT_ISSUER`     | `your-app`                                         | JWT issuer     |
| `JWT_AUDIENCE`   | `your-app`                                         | JWT audience   |

### 4.6 보안 권장 사항

운영 환경에서는 `.env`에서 다음을 반드시 변경하세요:

- `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`
- `JWT_SECRET_KEY`, `JWT_ISSUER`, `JWT_AUDIENCE`
- `REDIS_PASSWORD` (설정 시)

---

## 5. 실행 방법

### 5.1 인프라만 실행 (MySQL + Redis)

```bash
cd infra
docker compose up -d
```

MySQL, Redis가 기동되며, Backend 등 애플리케이션 없이 DB/캐시만 사용할 때 적합합니다.

### 5.2 Backend 포함 실행

```bash
cd infra
docker compose --profile backend up -d
```

MySQL, Redis, Backend가 순차 기동됩니다.

### 5.3 Frontend 포함 실행 (Backend 자동 포함)

```bash
cd infra
docker compose --profile frontend up -d
```

### 5.4 Mobile 포함 실행 (Backend 자동 포함)

```bash
cd infra
docker compose --profile mobile up -d
```

### 5.5 전체 앱 실행 (Backend + Frontend + Mobile)

```bash
cd infra
docker compose --profile app up -d
```

---

## 6. 프로파일별 사용법

### 6.1 프로파일 요약

| 명령어                 | 실행 서비스                             |
| ---------------------- | --------------------------------------- |
| `docker compose up -d` | app-db, app-redis                       |
| `--profile backend`    | + app-backend                           |
| `--profile frontend`   | + app-backend, app-frontend             |
| `--profile mobile`     | + app-backend, app-mobile               |
| `--profile app`        | + app-backend, app-frontend, app-mobile |
| `--profile with-nginx` | + app-nginx (리버스 프록시)             |

### 6.2 프로파일 조합 예시

```bash
# Backend + Frontend만
docker compose --profile frontend up -d

# Backend + Mobile만
docker compose --profile mobile up -d

# 전체 (인프라 + 앱)
docker compose --profile app up -d

# Nginx 리버스 프록시까지
docker compose --profile app --profile with-nginx up -d
```

---

## 7. 포트 및 접속 정보

### 7.1 기본 포트

| 서비스   | 포트 | 접속 정보                                      |
| -------- | ---- | ---------------------------------------------- |
| MySQL    | 3306 | `localhost:3306` (MYSQL_USER / MYSQL_PASSWORD) |
| Redis    | 6379 | `localhost:6379`                               |
| Backend  | 8080 | http://localhost:8080                          |
| Frontend | 5173 | http://localhost:5173                          |
| Mobile   | 5174 | http://localhost:5174                          |

### 7.2 Frontend / Mobile 접속 시

- Frontend: http://localhost:5173 → SPA, `/api` 요청은 Backend로 프록시
- Mobile: http://localhost:5174 → Flutter Web, `/api` 요청은 Backend로 프록시
- 두 앱 모두 같은 Backend(8080) API를 사용합니다.

---

## 8. 빌드 및 디버깅

### 8.1 이미지 재빌드

```bash
cd infra

# Backend만 재빌드
docker compose build app-backend

# Frontend만 재빌드
docker compose build app-frontend

# Mobile만 재빌드
docker compose build app-mobile

# 전체 재빌드 (캐시 없이)
docker compose build --no-cache
```

### 8.2 로그 확인

```bash
# MySQL 로그
docker compose logs -f app-db

# Redis 로그
docker compose logs -f app-redis

# Backend 로그
docker compose logs -f app-backend

# Frontend 로그
docker compose logs -f app-frontend

# Mobile 로그
docker compose logs -f app-mobile

# 전체 로그
docker compose logs -f
```

### 8.3 컨테이너 상태 확인

```bash
docker compose ps
```

### 8.4 컨테이너 내부 접속

```bash
# Backend 셸
docker exec -it app-backend sh

# MySQL CLI
docker exec -it app-db mysql -u app_user -p app_db

# Redis CLI
docker exec -it app-redis redis-cli

# Frontend nginx 설정 확인
docker exec -it app-frontend cat /etc/nginx/conf.d/default.conf
```

---

## 9. 문제 해결

### 9.1 MySQL / Redis 시작 실패

**증상**: app-db 또는 app-redis가 unhealthy이거나 시작되지 않음

**확인 사항**:

- `.env` 파일이 존재하는지 확인
- 포트 3306, 6379가 다른 프로세스에 의해 사용 중인지 확인
- MySQL 최초 기동 시 30초 이상 소요될 수 있음

```bash
docker compose logs app-db
docker compose logs app-redis
```

### 9.2 Backend 시작 실패

**증상**: app-backend가 재시작을 반복함

**확인 사항**:

- app-db, app-redis가 `healthy` 상태인지 확인
- `.env`의 `MYSQL_PASSWORD`가 Backend의 `DB_PASSWORD`와 일치하는지 확인
- Backend `application-prod.yml`의 환경 변수명과 docker-compose의 `environment`가 맞는지 확인

```bash
docker compose logs app-db
docker compose logs app-backend
```

### 9.3 Frontend/Mobile에서 API 호출 실패

**증상**: 로그인/API 호출 시 CORS 또는 연결 오류

**확인 사항**:

- app-backend가 정상 동작 중인지 확인
- 브라우저에서 http://localhost:5173 또는 http://localhost:5174 로 접속했는지 확인
- nginx 설정에서 `proxy_pass http://app-backend:8080`이 올바른지 확인

### 9.4 Flutter Web 빌드 실패

**증상**: app-mobile 빌드 시 Flutter 오류

**확인 사항**:

- Flutter Docker 이미지 다운로드 완료 여부
- `mobile/pubspec.yaml`의 `sdk` 버전이 Flutter 이미지와 호환되는지 확인

```bash
cd infra
docker compose build --no-cache app-mobile
```

### 9.5 포트 충돌

**증상**: `port is already allocated`

**해결**:

- `.env`에서 `BACKEND_PORT`, `FRONTEND_PORT`, `MOBILE_PORT`, `MYSQL_PORT`, `REDIS_PORT` 변경
- 또는 기존 프로세스 종료 후 재시작

### 9.6 볼륨/캐시 초기화

```bash
cd infra

# 컨테이너 + 볼륨 삭제 (MySQL/Redis 데이터 초기화)
docker compose down -v

# 이미지까지 삭제
docker compose down -v --rmi local

# 다시 시작
docker compose --profile app up -d
```

---

## 10. 참고 문서

- [DEPLOYMENT.md](./DEPLOYMENT.md) - **배포 가이드**: 운영 환경 변수 목록, 기동·헬스체크, RULE 5.3 (Step 20)
- [FLUTTER_API_INTEGRATION.md](./FLUTTER_API_INTEGRATION.md) - Flutter REST·인증·지도 연동 예시 (Step 20)
- [DEVELOPMENT_ENVIRONMENT.md](./DEVELOPMENT_ENVIRONMENT.md) - 로컬 개발환경 세팅
- [SERVER_STARTUP_GUIDE.md](./SERVER_STARTUP_GUIDE.md) - 서버 구동 및 프로파일별 DB 설정
- [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) - 일반 문제 해결
- `infra/README.md` - MySQL, Redis 상세 가이드 (infra 디렉터리 구성 시)

---

**Last Updated:** 2026-02-06
