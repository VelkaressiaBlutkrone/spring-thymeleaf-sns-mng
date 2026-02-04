# 전체 시스템 아키텍처 다이어그램

본 문서는 `doc/PRD.md`를 기준으로 **지도 기반 SNS 웹/모바일 통합 플랫폼**의 고수준 아키텍처를 정의한다.

## 구성요소

- **Web (Thymeleaf)**: 서버 렌더링 UI
- **Mobile (Flutter)**: REST API 기반 클라이언트
- **Backend (Spring Boot 4 / Java 21)**
  - Controller: REST API, Thymeleaf MVC 분리
  - Service: 비즈니스 로직, 트랜잭션 경계
  - Repository: 데이터 접근
  - Security: 인증/인가(기본 deny-by-default)
  - Validation / Global Exception Handler: 입력 검증, 예외 일관 처리
  - AOP: 로깅/메트릭 등 횡단 관심사(비즈니스 로직 금지)
- **MySQL**: 운영 DB
- **H2**: 개발용 DB(로컬)
- **Redis**: 세션/캐시/위치 기반 데이터
- **Map Provider**: Kakao/Naver/Google 중 추상화(교체 가능)
- **File Storage**: 이미지 저장소(로컬/NAS/S3 등)

## 다이어그램 (Mermaid)

```mermaid
flowchart LR
  subgraph Clients[클라이언트]
    W[Web<br/>(Thymeleaf)]
    M[Mobile<br/>(Flutter)]
  end

  subgraph BE[백엔드<br/>(Spring Boot 4 / Java 21)]
    C[Controllers<br/>(REST API, Thymeleaf MVC)]
    S[Services]
    R[Repositories]

    Sec[Security<br/>(AuthN/AuthZ)]
    Val[Validation]
    Ex[Global Exception Handler]
    AOP[AOP<br/>(Logging/Metrics/Tracing)]

    C --> S --> R
    C --> Val
    C --> Ex
    S --> Sec
    S --> AOP
  end

  subgraph Infra[외부/인프라]
    DB[(MySQL)]
    Cache[(Redis<br/>Session/Cache)]
    Map[Map Provider<br/>(Kakao/Naver/Google)]
    FS[(File Storage)]
  end

  W -- HTTPS --> C
  M -- HTTPS --> C

  R --> DB
  S --> Cache
  S --> Map
  S --> FS
```

## 주요 설계 원칙 (RULE 준수)

- **계층 분리**: Controller → Service → Repository 단방향
- **보안 기본값 차단**: 보호 리소스 deny-by-default
- **비밀정보 외부 주입**: DB/Redis 비밀번호 등은 환경 변수 또는 Secret Manager
- **AOP 사용 제한**: 로깅/메트릭 등 횡단 관심사만(업무 규칙 금지)

---

> 최종 업데이트: 2026-02-04

