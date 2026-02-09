# 지도 기반 SNS 웹/모바일 통합 플랫폼

위치 중심의 지도 UI와 게시글/이미지 SNS를 결합한 웹/모바일 통합 플랫폼입니다.

## 📋 프로젝트 개요

사용자의 위치를 기반으로 지도에서 게시글을 작성하고, Pin을 생성하며, 반경 내 정보를 실시간으로 확인할 수 있는 소셜 네트워크 서비스입니다.

### 주요 기능

- 🗺️ **지도 기반 핵심 기능**: 현재 위치 표시, 반경 내 정보 조회, Pin 생성 및 관리
- 📝 **게시판 시스템**: 일반 게시글 및 이미지 게시글 작성/조회/수정/삭제
- 📍 **Pin 관리**: 지도에 Pin 생성 및 게시글 연동
- 👤 **회원 시스템**: 이메일 기반 회원가입/로그인, JWT 인증
- 🔐 **관리자 기능**: 회원 관리, 게시물 관리, 통계 조회
- 📱 **모바일 지원**: Flutter 기반 크로스 플랫폼 앱

## 🛠️ 기술 스택

### Backend

| 구분         | 기술                                |
| ------------ | ----------------------------------- |
| 프레임워크   | Spring Boot 4.0.2                   |
| 언어         | Java 21                             |
| 웹           | Spring MVC, Thymeleaf               |
| 보안         | Spring Security, JWT (jjwt 0.12.x)  |
| 데이터       | Spring Data JPA, QueryDSL 5.1.0     |
| 데이터베이스 | MySQL 8.0 (운영), H2 (개발)         |
| 캐시/세션    | Redis 7, Redisson 3.40.2            |
| API 문서     | Swagger/OpenAPI 3 (springdoc 3.0.1) |
| 빌드         | Gradle                              |
| 테스트       | JUnit 5, Testcontainers, JaCoCo     |

### Frontend

| 구분     | 기술                                 |
| -------- | ------------------------------------ |
| 웹       | Thymeleaf, Bootstrap/Tailwind        |
| 모바일   | Flutter 3.10.8+                      |
| 지도 API | Kakao/Naver/Google Map (추상화 설계) |

### Infrastructure

| 구분         | 기술                   |
| ------------ | ---------------------- |
| 컨테이너     | Docker, Docker Compose |
| 데이터베이스 | MySQL 8.0              |
| 캐시         | Redis 7                |

## 📁 프로젝트 구조

```
spring_thymleaf_map_sns_mng/
├── src/main/java/com/example/sns/
│   ├── SpringThymleafMapSnsMngApplication.java  # 메인 애플리케이션
│   ├── config/                                   # 설정 (Security, JPA, Redis 등)
│   ├── controller/                               # REST API 및 Thymeleaf 컨트롤러
│   ├── service/                                  # 비즈니스 로직
│   ├── domain/                                   # 엔티티 및 Value Object
│   ├── repository/                               # JPA Repository
│   ├── dto/                                      # 요청/응답 DTO
│   ├── exception/                                # 예외 처리
│   ├── security/                                 # 보안 관련
│   ├── aop/                                      # 횡단 관심사 (로깅 등)
│   └── util/                                     # 유틸리티
├── src/main/resources/
│   ├── application.yml                           # 애플리케이션 설정
│   ├── templates/                                # Thymeleaf 템플릿
│   └── static/                                   # 정적 리소스
├── src/test/                                     # 테스트 코드
├── mobile/                                       # Flutter 모바일 앱
│   ├── lib/                                      # Flutter 소스
│   └── pubspec.yaml                              # Flutter 의존성
├── infra/                                        # Docker 인프라
│   ├── docker-compose.yml                        # Docker Compose 설정
│   ├── docker/                                   # Dockerfile들
│   └── .env.example                              # 환경 변수 예제
├── doc/                                          # 프로젝트 문서
│   ├── PRD.md                                    # 제품 요구사항 문서
│   ├── ARCHITECTURE.md                           # 아키텍처 설계
│   ├── API_SPEC.md                               # REST API 명세
│   ├── ERD.md                                    # 데이터베이스 설계
│   ├── AUTH_DESIGN.md                            # 인증 설계
│   ├── RULE.md                                   # 개발 규칙
│   ├── INFRA.md                                  # 인프라 가이드
│   ├── DEVELOPMENT_ENVIRONMENT.md                # 개발 환경 세팅
│   ├── SERVER_STARTUP_GUIDE.md                   # 서버 시작 가이드
│   └── TROUBLESHOOTING.md                        # 문제 해결
├── build.gradle                                  # Gradle 빌드 설정
└── README.md                                     # 프로젝트 소개 (본 문서)
```

## 🚀 빠른 시작

### 사전 요구사항

- Java 21
- Docker & Docker Compose
- Gradle (또는 내장 Gradle Wrapper 사용)
- Flutter 3.10.8+ (모바일 앱 실행 시)

### 1. 환경 설정

```bash
# 프로젝트 클론
git clone <repository-url>
cd spring_thymleaf_map_sns_mng

# 환경 변수 설정
cd infra
cp .env.example .env
# .env 파일을 열어 필요한 값 수정 (JWT_SECRET_KEY, DB 비밀번호 등)
# 지도 표시(Step 12): MAP_KAKAO_JS_APP_KEY (카카오 개발자 콘솔 → JavaScript 키)
```

### 2. 인프라 실행 (Docker)

```bash
# MySQL + Redis 실행
cd infra
docker compose up -d

# Backend 포함 실행
docker compose --profile backend up -d

# Backend + Mobile 모두 실행
docker compose --profile mobile up -d
```

### 3. 로컬 개발 실행

#### Backend

```bash
# Gradle Wrapper를 사용한 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/app.jar
```

#### Mobile (Flutter)

```bash
cd mobile
flutter pub get
flutter run -d chrome  # 웹 브라우저에서 실행
# 또는
flutter run  # 연결된 디바이스에서 실행
```

### 4. 접속 정보

| 서비스      | URL                                   | 비고                             |
| ----------- | ------------------------------------- | -------------------------------- |
| Backend API | http://localhost:8080                 | REST API 서버                    |
| Swagger UI  | http://localhost:8080/swagger-ui.html | API 문서                         |
| Mobile Web  | http://localhost:5174                 | Flutter Web                      |
| MySQL       | localhost:3306                        | app_user / .env의 MYSQL_PASSWORD |
| Redis       | localhost:6379                        | -                                |

## 📚 주요 문서

프로젝트의 상세한 정보는 `doc/` 디렉토리의 문서를 참조하세요:

- **[PRD.md](doc/PRD.md)**: 제품 요구사항 및 기능 명세
- **[ARCHITECTURE.md](doc/ARCHITECTURE.md)**: 시스템 아키텍처 설계
- **[API_SPEC.md](doc/API_SPEC.md)**: REST API 상세 명세
- **[ERD.md](doc/ERD.md)**: 데이터베이스 스키마 설계
- **[AUTH_DESIGN.md](doc/AUTH_DESIGN.md)**: JWT 기반 인증/인가 설계
- **[DEVELOPMENT_ENVIRONMENT.md](doc/DEVELOPMENT_ENVIRONMENT.md)**: 개발 환경 세팅 가이드
- **[SERVER_STARTUP_GUIDE.md](doc/SERVER_STARTUP_GUIDE.md)**: 서버 시작 가이드
- **[INFRA.md](doc/INFRA.md)**: Docker 인프라 상세 가이드
- **[RULE.md](doc/RULE.md)**: 개발 규칙 및 코딩 컨벤션
- **[TROUBLESHOOTING.md](doc/TROUBLESHOOTING.md)**: 문제 해결 가이드

## 🧪 테스트

### 단위 테스트

```bash
# 단위 테스트 실행 (통합 테스트 제외)
./gradlew test

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/html/index.html
```

### 통합 테스트

```bash
# Docker가 필요한 통합 테스트 실행
./gradlew integrationTest
```

## 🏗️ 아키텍처

### 계층 구조

```
Controller → Service → Repository
     ↓           ↓          ↓
    DTO       Domain     Domain
```

### 주요 도메인

- **User**: 회원 (일반 사용자 / 관리자)
- **Post**: 일반 게시글
- **ImagePost**: 이미지 게시글
- **Pin**: 지도 Pin
- **Location**: 위도/경도 Value Object

### 보안

- **인증**: JWT (Access Token + Refresh Token)
- **인가**: Spring Security 기반 역할 기반 접근 제어 (RBAC)
- **세션**: Redis 기반 세션 관리
- **비밀번호**: BCrypt 해싱

## 🔑 주요 API 엔드포인트

### 인증

- `POST /api/auth/login` - 로그인
- `POST /api/auth/refresh` - 토큰 갱신
- `POST /api/auth/logout` - 로그아웃
- `GET /api/auth/me` - 현재 사용자 정보

### 회원

- `POST /api/members` - 회원가입
- `GET /api/members/{id}` - 회원 조회

### 게시글

- `GET /api/posts` - 게시글 목록
- `POST /api/posts` - 게시글 작성
- `GET /api/posts/{id}` - 게시글 상세
- `PUT /api/posts/{id}` - 게시글 수정
- `DELETE /api/posts/{id}` - 게시글 삭제

### Pin

- `GET /api/pins` - Pin 목록
- `POST /api/pins` - Pin 생성
- `GET /api/pins/nearby` - 반경 내 Pin 조회

### 관리자
/api/pins` - Pin 목록
- `POST /api/pins` - Pin 생성
- `GET /api/pins/nearby` - 반경 내 Pin 조회

### 관리자

- `GET /api/admin/members` - 회원 관리
- `GET /api/admin/posts` - 게시물 관리
- `GET /api/admin/stats/*` - 통계 조회

자세한 API 명세는 [API_SPEC.md](doc/API_SPEC.md) 또는 Swagger UI를 참조하세요.

## 🌟 주요 기능 상세

### 지도 기반 기능

1. **현재 위치 표시**: GPS를 통한 사용자 위치 수집 및 지도 중심 설정
2. **반경 내 정보 조회**: 특정 위치 기준 반경 내 Pin 및 게시글 조회
3. **Pin 생성 및 관리**: 지도에 Pin 생성, 게시글과 연동
4. **경로 및 거리 계산**: 사용자 위치에서 목적지까지의 거리 및 경로 표시

### 게시판 기능

1. **일반 게시글**: 제목, 내용, 위치 정보 포함
2. **이미지 게시글**: 이미지 업로드 및 텍스트 결합
3. **위치 연동**: 게시글 작성 시 위치 정보 저장 및 지도 표시
4. **Pin 연동**: 게시글과 Pin 연결

### 관리자 기능

1. **회원 관리**: 전체 회원 조회, 추가, 수정, 삭제
2. **게시물 관리**: 게시글 조회, 수정, 삭제, 공지 등록
3. **통계**: 가입/로그인/게시글 통계 (일/주/월/분기/년)

## 🔧 개발 원칙

- **실무 기준**: 프로덕션 수준의 코드 및 설계
- **확장성 우선**: OAuth2, WebSocket, 알림 등 확장 고려
- **테스트 가능**: 단위/통합 테스트 가능한 구조
- **운영 기준**: MySQL, Redis 기준 설계 및 설정
- **계층 분리**: Controller → Service → Repository 단방향 의존성
- **보안 기본 차단**: deny-by-default 원칙

## 📝 라이선스

이 프로젝트는 학습 및 포트폴리오 목적으로 제작되었습니다.

## 👥 기여

프로젝트에 기여하고 싶으시다면 Pull Request를 보내주세요.

## 📞 문의

프로젝트 관련 문의사항은 이슈를 등록해 주세요.

---

> **문서 버전**: 1.0.0
> **최종 업데이트**: 2026-02-06
