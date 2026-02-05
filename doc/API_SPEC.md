# REST API 명세서

> PRD 3(핵심 기능), ERD, RULE 2.1(API 설계 규칙) 기준.

## 1. 개요

| 항목 | 내용 |
|------|------|
| Base URL | `/api` |
| 인증 | 세션·쿠키 기반 (Redis 저장) |
| 응답 형식 | JSON |
| 공통 에러 | `ErrorResponse` (code, message, fieldErrors) |

---

## 2. 회원 (Members)

### 2.1 회원가입

| 항목 | 내용 |
|------|------|
| Method | `POST` |
| URL | `/api/members` |
| 인증 | 불필요 |
| Request Body | `MemberJoinRequest` |
| Response | `201 Created` + `MemberResponse` |

**MemberJoinRequest**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | string | O | 이메일 (형식 검증) |
| password | string | O | 비밀번호 (8자 이상 등) |
| nickname | string | O | 닉네임 |

**Response (201)**

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "닉네임",
  "role": "USER",
  "createdAt": "2026-02-04T00:00:00"
}
```

### 2.2 회원 조회 (상세)

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/members/{id}` |
| 인증 | 로그인 권장 (본인만 일부 필드 상세) |
| Path | `id` - 회원 ID |
| Response | `200 OK` + `MemberResponse` |

---

## 3. 인증 (Auth)

### 3.1 로그인

| 항목 | 내용 |
|------|------|
| Method | `POST` |
| URL | `/api/auth/login` |
| 인증 | 불필요 |
| Request Body | `LoginRequest` |
| Response | `200 OK` (세션 생성, Set-Cookie) 또는 `401 Unauthorized` |

**LoginRequest**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | string | O | 이메일 |
| password | string | O | 비밀번호 |

### 3.2 로그아웃

| 항목 | 내용 |
|------|------|
| Method | `POST` |
| URL | `/api/auth/logout` |
| 인증 | 로그인 필수 |
| Response | `200 OK` (세션 무효화) |

### 3.3 현재 사용자 조회

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/auth/me` |
| 인증 | 로그인 필수 |
| Response | `200 OK` + `MemberResponse` 또는 `401` |

---

## 4. 게시글 (Posts)

### 4.1 게시글 목록

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/posts` |
| 인증 | 불필요 |
| Query | `page`, `size`, `keyword` (선택) |
| Response | `200 OK` + `Page<PostResponse>` |

### 4.2 게시글 상세

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/posts/{id}` |
| 인증 | 불필요 |
| Path | `id` - 게시글 ID |
| Response | `200 OK` + `PostResponse` |

### 4.3 게시글 작성

| 항목 | 내용 |
|------|------|
| Method | `POST` |
| URL | `/api/posts` |
| 인증 | 로그인 필수 |
| Request Body | `PostCreateRequest` |
| Response | `201 Created` + `PostResponse` |

**PostCreateRequest**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | string | O | 제목 |
| content | string | O | 내용 |
| latitude | number | X | 위도 (위치 선택 시) |
| longitude | number | X | 경도 |
| pinId | long | X | 연결할 Pin ID (nullable) |

### 4.4 게시글 수정

| 항목 | 내용 |
|------|------|
| Method | `PUT` |
| URL | `/api/posts/{id}` |
| 인증 | 로그인 필수, 작성자만 |
| Request Body | `PostUpdateRequest` |
| Response | `200 OK` + `PostResponse` 또는 `403 Forbidden` |

### 4.5 게시글 삭제

| 항목 | 내용 |
|------|------|
| Method | `DELETE` |
| URL | `/api/posts/{id}` |
| 인증 | 로그인 필수, 작성자만 |
| Response | `204 No Content` 또는 `403` |

---

## 5. 이미지 게시글 (ImagePosts)

### 5.1 이미지 게시글 목록

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/image-posts` |
| 인증 | 불필요 |
| Query | `page`, `size`, `keyword` |
| Response | `200 OK` + `Page<ImagePostResponse>` |

### 5.2 이미지 게시글 상세

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/image-posts/{id}` |
| 인증 | 불필요 |
| Response | `200 OK` + `ImagePostResponse` |

### 5.3 이미지 게시글 작성 (Multipart)

| 항목 | 내용 |
|------|------|
| Method | `POST` |
| URL | `/api/image-posts` |
| 인증 | 로그인 필수 |
| Content-Type | `multipart/form-data` |
| Form Fields | `title`, `content`, `image` (파일), `latitude`, `longitude`, `pinId` |
| Response | `201 Created` + `ImagePostResponse` |

### 5.4 이미지 게시글 수정

| 항목 | 내용 |
|------|------|
| Method | `PUT` |
| URL | `/api/image-posts/{id}` |
| 인증 | 로그인 필수, 작성자만 |
| Response | `200 OK` 또는 `403` |

### 5.5 이미지 게시글 삭제

| 항목 | 내용 |
|------|------|
| Method | `DELETE` |
| URL | `/api/image-posts/{id}` |
| 인증 | 로그인 필수, 작성자만 |
| Response | `204 No Content` 또는 `403` |

---

## 6. Pin

### 6.1 Pin 목록 (사용자별)

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/pins` |
| 인증 | 로그인 필수 |
| Query | `page`, `size` |
| Response | `200 OK` + `Page<PinResponse>` |

### 6.2 Pin 상세

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/pins/{id}` |
| 인증 | 로그인 필수, 소유자만 |
| Response | `200 OK` + `PinResponse` |

### 6.3 Pin 생성

| 항목 | 내용 |
|------|------|
| Method | `POST` |
| URL | `/api/pins` |
| 인증 | 로그인 필수 |
| Request Body | `PinCreateRequest` |
| Response | `201 Created` + `PinResponse` |

**PinCreateRequest**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| latitude | number | O | 위도 |
| longitude | number | O | 경도 |
| description | string | X | 설명 |

### 6.4 Pin 수정

| 항목 | 내용 |
|------|------|
| Method | `PUT` |
| URL | `/api/pins/{id}` |
| 인증 | 로그인 필수, 소유자만 |
| Request Body | `PinUpdateRequest` |
| Response | `200 OK` 또는 `403` |

### 6.5 Pin 삭제

| 항목 | 내용 |
|------|------|
| Method | `DELETE` |
| URL | `/api/pins/{id}` |
| 인증 | 로그인 필수, 소유자만 |
| Response | `204 No Content` 또는 `403` |

### 6.6 반경 내 Pin 조회

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/pins/nearby` |
| 인증 | 불필요 |
| Query | `lat`, `lng`, `radiusKm`, `page`, `size` |
| Response | `200 OK` + `Page<PinResponse>` |

---

## 7. 마이페이지 (Me)

### 7.1 내 게시글 목록

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/me/posts` |
| 인증 | 로그인 필수 |
| Query | `page`, `size` |
| Response | `200 OK` + `Page<PostResponse>` |

### 7.2 내 이미지 게시글 목록

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/me/image-posts` |
| 인증 | 로그인 필수 |
| Query | `page`, `size` |
| Response | `200 OK` + `Page<ImagePostResponse>` |

### 7.3 내 Pin 목록

| 항목 | 내용 |
|------|------|
| Method | `GET` |
| URL | `/api/me/pins` |
| 인증 | 로그인 필수 |
| Query | `page`, `size` |
| Response | `200 OK` + `Page<PinResponse>` |

### 7.4 개인정보 수정

| 항목 | 내용 |
|------|------|
| Method | `PUT` |
| URL | `/api/me` |
| 인증 | 로그인 필수 |
| Request Body | `MemberUpdateRequest` (nickname 등) |
| Response | `200 OK` + `MemberResponse` |

---

## 8. 관리자 (Admin) — ROLE_ADMIN 필수

### 8.1 회원 관리

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/admin/members` | 회원 목록 (페이징·검색) |
| GET | `/api/admin/members/{id}` | 회원 상세 |
| POST | `/api/admin/members` | 회원 추가 |
| PUT | `/api/admin/members/{id}` | 회원 수정 |
| DELETE | `/api/admin/members/{id}` | 회원 삭제 |

### 8.2 게시물 관리

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/admin/posts` | 게시글 목록 (페이징·검색) |
| PUT | `/api/admin/posts/{id}` | 게시글 수정 |
| DELETE | `/api/admin/posts/{id}` | 게시글 삭제 |
| PATCH | `/api/admin/posts/{id}/notice` | 공지 등록/해제 |

### 8.3 통계

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/admin/stats/signup` | 가입 통계 (기간별) |
| GET | `/api/admin/stats/login` | 로그인 통계 (기간별) |
| GET | `/api/admin/stats/posts` | 글 통계 (일/주/월/분기/년) |

**Query 파라미터 (통계 공통)**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| startDate | string (yyyy-MM-dd) | 시작일 |
| endDate | string (yyyy-MM-dd) | 종료일 |

---

## 9. 공통 응답 스키마

### 9.1 ErrorResponse

```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력값 검증 실패",
  "fieldErrors": [
    { "field": "email", "value": "invalid", "reason": "이메일 형식이 올바르지 않습니다." }
  ]
}
```

### 9.2 Page\<T\>

```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## 10. HTTP 상태 코드

| 코드 | 의미 |
|------|------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request (검증 실패 등) |
| 401 | Unauthorized (미인증) |
| 403 | Forbidden (권한 부족) |
| 404 | Not Found |
| 500 | Internal Server Error |

---

> **문서 버전**: 1.0.0
> **기준**: PRD 3, ERD, RULE 2.1
> **최종 업데이트**: 2026-02-04
