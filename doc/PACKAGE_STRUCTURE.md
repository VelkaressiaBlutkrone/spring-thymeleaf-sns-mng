# 패키지 구조 (Step 3)

> RULE 3.1: Controller → Service → Repository 단방향 의존성

## 계층 구조

```text
com.example.sns/
├── config/           # 설정 (Security, JPA, QueryDSL, MapProperties 등)
├── controller/       # REST API 핸들러
├── service/          # 비즈니스 로직 (트랜잭션 경계)
│   └── map/          # 지도 API 추상화 (MapService, GoogleMapServiceImpl, NoOpMapService)
├── domain/           # 엔티티, Value Object
├── repository/       # JPA Repository 인터페이스
├── dto/
│   ├── request/     # 요청 DTO
│   └── response/    # 응답 DTO
├── exception/        # 예외, GlobalExceptionHandler
└── aop/              # 횡단 관심사 (로깅, 감사)
```

## 의존성 방향

```text
Controller → Service → Repository
     ↓           ↓          ↓
    DTO       Domain     Domain
```

## 도메인 (domain/)

| 클래스 | 설명 |
|--------|------|
| User | 회원 (ROLE_USER/ROLE_ADMIN) |
| Post | 일반 게시글 |
| ImagePost | 이미지 게시글 |
| Pin | 지도 Pin |
| Location | 위도/경도 Value Object (@Embeddable) |
| BaseEntity | 감사 컬럼 (created_at, updated_at) |
| UserRole | 역할 enum |

## Repository (repository/)

| 인터페이스 | 설명 |
|------------|------|
| UserRepository | 회원 CRUD, findByEmail |
| PostRepository | 게시글 CRUD, 목록(공지 우선) |
| ImagePostRepository | 이미지 게시글 CRUD, 목록 |
| PinRepository | Pin CRUD, 사용자별 목록, 반경 내 조회 (Step 11) |

## DTO (dto/)

| 클래스 | 용도 |
|--------|------|
| UserResponse | 회원 목록/상세 응답 |
| PostResponse | 게시글 목록/상세 응답 |
| ImagePostResponse | 이미지 게시글 목록/상세 응답 |
| PinResponse | Pin 응답 |

---

> 최종 업데이트: 2026-02-09
