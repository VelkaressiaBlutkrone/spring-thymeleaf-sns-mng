# 개발환경 세팅 가이드

본 문서는 Spring Boot 기반 프로젝트의 개발환경 설정을 위한 **공통 가이드**입니다. 필수 도구의 버전, 설치 방법, 경로 설정을 정의합니다. 프로젝트에 맞게 적용하세요.

---

## 목차

1. [개요](#1-개요)
2. [백엔드 환경 (Java, Spring, Gradle)](#2-백엔드-환경-java-spring-gradle)
3. [프론트엔드 환경 (Node.js, npm, React, TypeScript)](#3-프론트엔드-환경-nodejs-npm-react-typescript)
4. [모바일 환경 (Dart, Flutter)](#4-모바일-환경-dart-flutter)
5. [인프라 환경 (MySQL, Redis, Docker)](#5-인프라-환경-mysql-redis-docker)
6. [환경 변수 및 경로 설정](#6-환경-변수-및-경로-설정)
7. [IDE 및 에디터 설정](#7-ide-및-에디터-설정)
8. [환경 검증 방법](#8-환경-검증-방법)

---

## 1. 개요

### 1.1 프로젝트 기술 스택 버전 요약

| 구분           | 도구        | 권장 버전                | 비고                    |
| -------------- | ----------- | ------------------------ | ----------------------- |
| **백엔드**     | Java (JDK)  | 21                       | 필수                    |
|                | Spring Boot | 4.x / 3.x                | Gradle로 관리           |
|                | Gradle      | 8.x / 9.x                | Wrapper 사용            |
| **프론트엔드** | Node.js     | 18.x 이상 (권장: 20 LTS) | npm 포함                |
|                | npm         | 9.x 이상                 | Node.js와 함께 설치     |
|                | React       | 18.x / 19.x              | package.json            |
|                | TypeScript  | 5.x                      | package.json            |
|                | Vite        | 5.x / 7.x                | 빌드 도구               |
| **모바일**     | Dart SDK    | ^3.x                     | Flutter와 함께 설치     |
|                | Flutter     | 3.x 이상                 | pubspec.yaml            |
| **인프라**     | MySQL       | 8.0                      | 운영/로컬 DB            |
|                | Redis       | 7.x                      | 캐시, 세션, 토큰 스토어 |
|                | Docker      | 24.x 이상                | 선택적 사용             |

### 1.2 개발 환경별 데이터베이스

- **개발 (dev)**: H2 인메모리 DB, Redis 미사용 (별도 DB 설치 불필요)
- **운영 (prod)**: MySQL 8.0, Redis (세션·캐시·토큰 스토어·분산 락 등)

---

## 2. 백엔드 환경 (Java, Spring, Gradle)

### 2.1 Java (JDK 21)

#### 버전 요구사항

- **Java 21** (LTS) 이상
- `build.gradle`에서 `JavaLanguageVersion.of(21)` 사용

#### 설치 방법

**Windows (Chocolatey):**

```powershell
choco install openjdk21
```

**Windows (수동):**

1. [Adoptium Eclipse Temurin](https://adoptium.net/) 또는 [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)에서 JDK 21 다운로드
2. 설치 후 환경 변수 설정 (아래 [6. 환경 변수 및 경로 설정](#6-환경-변수-및-경로-설정) 참고)

**macOS (Homebrew):**

```bash
brew install openjdk@21
```

**Linux (Ubuntu/Debian):**

```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

#### 경로 설정

- `JAVA_HOME`: JDK 설치 루트 (예: `C:\Program Files\Eclipse Adoptium\jdk-21`)
- `PATH`에 `%JAVA_HOME%\bin` 추가

#### 검증

```bash
java -version
# openjdk version "21.x.x" ...

javac -version
# javac 21.x.x
```

---

### 2.2 Spring Boot

Spring Boot는 Gradle 의존성으로 관리되므로 별도 설치 불필요합니다.

- **버전**: build.gradle에서 관리
- **설정 위치**: `build.gradle` → `id 'org.springframework.boot' version 'x.x.x'`

---

### 2.3 Gradle

프로젝트에 Gradle Wrapper가 포함되어 있어 별도 설치 없이 사용 가능합니다.

- **설정 위치**: `gradle/wrapper/gradle-wrapper.properties`
- **사용 방법**: `./gradlew` (Linux/macOS), `gradlew.bat` (Windows)

#### 직접 설치 (선택 사항)

**Windows (Chocolatey):**

```powershell
choco install gradle
```

**macOS (Homebrew):**

```bash
brew install gradle
```

---

## 3. 프론트엔드 환경 (Node.js, npm, React, TypeScript)

> **참고**: 프로젝트에 `frontend/` 디렉터리가 있는 경우에만 해당됩니다.

### 3.1 Node.js

#### 버전 요구사항

- **Node.js 18.x 이상** (권장: **20 LTS**)
- Vite, React 호환 버전 사용

#### 설치 방법

**Windows (공식 installer):**

1. [nodejs.org](https://nodejs.org/)에서 LTS 버전 다운로드
2. 설치 시 "Add to PATH" 옵션 체크

**Windows (Chocolatey):**

```powershell
choco install nodejs-lts
```

**macOS (Homebrew):**

```bash
brew install node@20
```

**nvm 사용 (Windows/macOS/Linux):**

```bash
nvm install 20
nvm use 20
```

#### 검증

```bash
node -v
# v20.x.x

npm -v
# 10.x.x 또는 9.x.x
```

---

### 3.2 npm, React, TypeScript, Vite

프로젝트 `frontend/` 디렉터리에서 npm으로 설치합니다.

```bash
cd frontend
npm install
```

---

## 4. 모바일 환경 (Dart, Flutter)

> **참고**: 프로젝트에 `mobile/` 디렉터리가 있는 경우에만 해당됩니다.

### 4.1 Flutter (Dart 포함)

Flutter를 설치하면 Dart SDK가 함께 설치됩니다.

#### 버전 요구사항

- **Flutter**: `mobile/pubspec.yaml` 환경에 맞는 버전
- **Dart SDK**: pubspec.yaml의 `sdk` 제약 조건 준수

#### 설치 방법

**Windows:**

1. [Flutter 공식 사이트](https://docs.flutter.dev/get-started/install/windows)에서 SDK 다운로드
2. 압축 해제 후 원하는 경로에 배치 (예: `C:\src\flutter`)
3. `PATH`에 `flutter/bin` 추가

**macOS:**

```bash
brew install --cask flutter
```

**Linux:**

```bash
git clone https://github.com/flutter/flutter.git -b stable
export PATH="$PATH:`pwd`/flutter/bin"
```

#### 경로 설정

- `PATH`에 `flutter/bin` 디렉터리 추가
- 예: `C:\src\flutter\bin` (Windows)

#### 검증

```bash
flutter doctor
```

#### Android 빌드 시 Java 버전

- 모바일 Android 빌드: **Java 17** (필요 시 `mobile/android/app/build.gradle.kts` 참조)
- 백엔드: Java 21 사용
- 둘 다 설치하고 `JAVA_HOME`을 필요한 쪽에 맞게 전환 가능

---

## 5. 인프라 환경 (MySQL, Redis, Docker)

### 5.1 MySQL 8.0

운영(prod) 프로파일 및 로컬 통합 테스트 시 사용합니다.

#### 설치 방법

**Docker (권장):**

```bash
cd infra
docker compose up -d app-db
```

> **참고**: 서비스명은 프로젝트에 따라 `app-db`, `{project}-db` 등으로 다를 수 있습니다. [INFRA.md](./INFRA.md) 참고.

**Windows (수동):**

- [MySQL Community Server](https://dev.mysql.com/downloads/mysql/) 다운로드 후 설치

**macOS:**

```bash
brew install mysql@8.0
brew services start mysql@8.0
```

**접속 정보 (예시, 프로젝트별로 다름):**

- Host: `localhost`
- Port: `3306`
- Database: `app_db` (또는 프로젝트에서 정의한 DB명)
- User: `app_user` (또는 프로젝트에서 정의한 사용자)
- Password: `.env` 또는 `application-prod.yml`에서 설정

---

### 5.2 Redis 7.x

세션·캐시·토큰 스토어·분산 락 등에 사용됩니다.

#### 설치 방법

**Docker (권장):**

```bash
cd infra
docker compose up -d app-redis
```

**Windows (수동):**

- [Redis for Windows](https://github.com/microsoftarchive/redis/releases) 또는 WSL2에서 Linux Redis 사용

**macOS:**

```bash
brew install redis
brew services start redis
```

**Linux:**

```bash
sudo apt install redis-server
sudo systemctl start redis
```

**접속 정보:**

- Host: `localhost`
- Port: `6379`

---

### 5.3 Docker (선택)

MySQL, Redis를 컨테이너로 실행할 때 사용합니다.

- **Docker Desktop**: [docker.com](https://www.docker.com/products/docker-desktop/)
- **설정**: `infra/docker-compose.yml` 사용
- **실행**: `docker compose up -d` (infra 디렉터리에서)
- **상세 가이드**: [INFRA.md](./INFRA.md)

---

## 6. 환경 변수 및 경로 설정

### 6.1 Windows (PowerShell/시스템 환경 변수)

| 변수명      | 예시 값                                    | 설명                        |
| ----------- | ------------------------------------------ | --------------------------- |
| `JAVA_HOME` | `C:\Program Files\Eclipse Adoptium\jdk-21` | JDK 설치 경로               |
| `Path`      | `%JAVA_HOME%\bin` 추가                     | Java 실행 파일              |
| `Path`      | `C:\Program Files\nodejs\`                 | Node.js (설치 시 자동 추가) |
| `Path`      | `C:\src\flutter\bin`                       | Flutter (수동 추가)         |

**PowerShell에서 임시 설정:**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### 6.2 macOS / Linux (bash/zsh)

`~/.zshrc` 또는 `~/.bashrc`에 추가:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home  # macOS 예시
export PATH=$JAVA_HOME/bin:$PATH
export PATH=$PATH:$HOME/flutter/bin
```

---

## 7. IDE 및 에디터 설정

### 7.1 권장 확장 (VS Code / Cursor)

- **Java**: Extension Pack for Java
- **Spring Boot**: Spring Boot Extension Pack
- **TypeScript/React**: ESLint, Prettier (frontend 프로젝트 시)
- **Dart/Flutter**: Flutter, Dart (mobile 프로젝트 시)
- **Docker**: Docker

### 7.2 인코딩 (Windows 한글 깨짐 방지)

PowerShell에서:

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001
```

---

## 8. 환경 검증 방법

### 8.1 전체 검증 체크리스트

```bash
# 1. Java
java -version
javac -version

# 2. Gradle (프로젝트 루트에서)
./gradlew --version   # Windows: gradlew.bat --version

# 3. Node.js / npm (frontend 프로젝트 시)
node -v
npm -v

# 4. 프론트엔드 의존성 (frontend 프로젝트 시)
cd frontend && npm install && npm run build

# 5. Flutter (mobile 프로젝트 시)
flutter doctor

# 6. Redis (실행 중인 경우)
redis-cli ping
# PONG

# 7. 백엔드 실행
./gradlew bootRun
```

### 8.2 빠른 실행 확인

**백엔드 (로컬/개발 프로파일, H2 사용):**

```bash
./gradlew bootRun
# 기본 포트: 8080
```

**프론트엔드 (frontend 프로젝트 시):**

```bash
cd frontend
npm install
npm run dev
# 기본 포트: 5173, API 프록시: http://localhost:8080
```

**Docker 인프라 (MySQL + Redis):**

```bash
cd infra
docker compose up -d
```

**Docker 앱 전체 (Backend + Frontend + Mobile):**

```bash
cd infra
docker compose --profile app up -d
```

→ 상세: [INFRA.md](./INFRA.md)

---

## 참고 문서

- [INFRA.md](./INFRA.md) - Docker 인프라 구축 (MySQL, Redis, Backend, Frontend, Mobile)
- [SERVER_STARTUP_GUIDE.md](./SERVER_STARTUP_GUIDE.md) - 서버 구동 및 DB 프로파일 설정
- [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) - 문제 해결

---

**Last Updated:** 2026-02-06
