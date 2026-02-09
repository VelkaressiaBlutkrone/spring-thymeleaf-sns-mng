# 지도 API 추상화 (Step 11)

> PRD 2.2: Kakao/Naver/Google Map API 추상화(교체 가능).
> RULE 3.4: 외부 호출 시 Timeout·Retry 정책.

## 1. 개요

| 항목 | 내용 |
|------|------|
| 인터페이스 | `MapService` |
| 구현체 | `NoOpMapService` (기본), `GoogleMapServiceImpl` |
| 선택 | `app.map.provider` 프로퍼티로 구현체 선택 |

## 2. 구현체 선택

| provider | 구현체 | 용도 |
|----------|--------|------|
| `none` (기본) | NoOpMapService | 외부 API 미사용. Geocoding/Reverse Geocoding 항상 empty |
| `google` | GoogleMapServiceImpl | Google Maps Geocoding API |

향후 `kakao`, `naver` 구현체 추가 가능.

## 3. Timeout·Retry 정책

| 설정 | 기본값 | 설명 |
|------|--------|------|
| `app.map.timeout-seconds` | 5 | Geocoding API 호출 타임아웃(초) |
| `app.map.retry-count` | 2 | 실패 시 재시도 횟수 |

Google Maps Java 클라이언트(`GeoApiContext.Builder`)에 적용:
- `connectTimeout(timeoutSeconds, TimeUnit.SECONDS)`
- `readTimeout(timeoutSeconds, TimeUnit.SECONDS)`
- `maxRetries(retryCount)`

## 4. 환경 변수

| provider | 필수 환경 변수 |
|----------|----------------|
| none | 없음 |
| google | `MAP_GOOGLE_API_KEY` (또는 `app.map.google-api-key`) |
| kakao | `MAP_KAKAO_API_KEY` (추가 구현 시) |
| naver | `MAP_NAVER_CLIENT_ID`, `MAP_NAVER_CLIENT_SECRET` (추가 구현 시) |

RULE 1.1: API Key·비밀정보는 환경 변수로만 주입.

## 5. 반경 조회 API

반경 내 Pin·게시글·이미지 게시글 조회는 **외부 지도 API 호출 없이** DB의 Haversine 공식으로 처리.

| API | URL | Query |
|-----|-----|-------|
| 반경 내 Pin | GET /api/pins/nearby | lat, lng, radiusKm, page, size |
| 반경 내 게시글 | GET /api/posts/nearby | lat, lng, radiusKm, page, size |
| 반경 내 이미지 게시글 | GET /api/image-posts/nearby | lat, lng, radiusKm, page, size |

## 6. Step 12~13: 지도 웹 UI (프론트엔드)

| 항목 | 내용 |
|------|------|
| 지도 JS | Kakao Maps JavaScript API (kakaoJsAppKey) |
| 설정 | `app.map.kakao-js-app-key` (환경 변수 `MAP_KAKAO_JS_APP_KEY`) |
| 용도 | 메인 지도, 마커, 인포윈도우, 게시글 작성 폼 위치 선택, 상세 지도·거리 |
| 웹 페이지 | `/` (지도), `/login`, `/posts/create`, `/image-posts/create`, `/posts/{id}`, `/image-posts/{id}` |
| 거리 계산 | 클라이언트 Haversine (post-detail-map.js), 사용자 위치→목적지 |

## 7. 로깅 (RULE 1.4.3)

- 외부 API 호출·실패 시 파라미터화 로깅 (`{}` placeholder)
- 비밀정보(API Key, 토큰) 로그 출력 금지

---

**최종 업데이트**: 2026-02-09
