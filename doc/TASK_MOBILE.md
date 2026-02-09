# 지도 기반 SNS 모바일 앱 — MobileTask (Flutter 단계별 작업)

- **기준 문서**: `doc/PRD.md`, `doc/RULE.md`, `doc/API_SPEC.md`, `doc/AUTH_DESIGN.md`
- **총 기간**: 50일 내외
- **총 Step**: 10단계

---

## 주석 규칙 (RULE 4.4)

- 모든 Step 산출물은 **RULE 4.4(주석 규칙)** 준수
- public API → 문서화 주석 필수 (한 줄 요약 → 상세 → @param/@return)
- 주석은 **한글** 기본, Why(이유) 설명, 코드와 동기화

---

## Step 템플릿 설명 (RULE 4.3.2)

> 신규 Step 작성·추가 시 **RULE 4.3.2(Task 문서 작성 구조)** 를 반드시 준수한다.

| 필드               | 설명                                     |
| ------------------ | ---------------------------------------- |
| **Step Name**      | 단계 이름                                |
| **Step Goal**      | 단계 완료 시 달성할 목표(한 문장)        |
| **Input**          | 이 단계에 필요한 입력(문서·코드·환경 등) |
| **Scope**          | 포함/제외 범위로 단계 경계 명확화        |
| **Instructions**   | 수행할 작업 목록                         |
| **Output Format**  | 산출물 형태·위치·형식                    |
| **Constraints**    | 반드시 지켜야 할 제약(RULE·기술 등)      |
| **Done When**      | 아래 조건 충족 시 단계 완료로 간주       |
| **Duration**       | 예상 소요 일수                           |
| **RULE Reference** | 참조할 RULE.md 섹션                      |

---

## 일정 요약

| Phase     | Step 범위  | 기간(일) | 비고                                       |
| --------- | ---------- | -------- | ------------------------------------------ |
| 기반·인증 | Step 1 ~ 3 | 12       | 프로젝트 셋업, API 클라이언트, 회원·로그인 |
| 게시판    | Step 4 ~ 5 | 8        | 네비게이션, 게시글 목록·상세               |
| 지도·Pin  | Step 6 ~ 7 | 12       | 지도 SDK, 위치, Pin·반경 조회              |
| 작성·마이 | Step 8 ~ 9 | 12       | 게시글/이미지 작성·수정, 마이페이지        |
| 마무리    | Step 10    | 6        | About, 테스트, 배포 준비                   |
| **합계**  | **10**     | **50**   |                                            |

---

## Step 1 — 프로젝트 셋업·아키텍처·패키지 구조

**Step Name:** 프로젝트 셋업·아키텍처·패키지 구조

**Step Goal:** Flutter 프로젝트 환경을 확정하고, `doc/` 문서를 참고한 모바일 아키텍처·패키지 구조를 설계하여 이후 단계의 공통 기준을 마련한다.

**Input:**

- PRD 2.3(Mobile: Flutter, REST API, 지도 SDK)
- doc/ARCHITECTURE.md, doc/API_SPEC.md
- 기존 `mobile/` Flutter 프로젝트

**Scope:**

- 포함: Flutter SDK·의존성 설정, `lib/` 패키지 구조 설계(domain/data/presentation 등), 환경 변수·설정 구조, base URL 등 API 기본 설정
- 제외: 실제 API 호출 구현, 지도 SDK 상세 연동, 비즈니스 로직

**Instructions:**

- `pubspec.yaml`에 Dio, flutter_secure_storage, geolocator 등 필수 의존성 추가
- `lib/` 하위 패키지 구조 설계: `core/`(공통), `data/`(API·저장소), `domain/`(모델·유스케이스), `presentation/`(화면·위젯)
- API Base URL·환경별 설정(dev/prod) 구조화(dart-define 또는 flavors)
- `doc/API_SPEC.md` 기반 DTO·모델 클래스 골격 정의(Member, Post, Pin, ImagePost 등)
- README 또는 `mobile/README.md`에 빌드·실행 방법, 환경 변수 문서화

**Output Format:**

- 패키지 구조: `lib/` 하위 디렉토리 및 예시 파일
- 설정: `lib/core/config/` 등 환경 설정
- 문서: `mobile/README.md` 또는 `doc/MOBILE_ARCHITECTURE.md`

**Constraints:**

- RULE 1.1(비밀정보 환경 변수 주입) 준수 — API Key·base URL은 환경 변수 또는 dart-define
- `lib/` 구조는 계층 분리 원칙(3.1)에 맞게 설계

**Done When:**

- Flutter `flutter run` 성공, 패키지 구조·DTO 골격이 문서화되어 있고, 환경별 설정 구조가 확정된다.

**Duration:** 4일

**RULE Reference:** 1.1, 3.1, 4.3.2

---

## Step 2 — API 클라이언트·인증·토큰 관리

**Step Name:** API 클라이언트·인증·토큰 관리

**Step Goal:** Backend REST API 연동을 위한 Dio 기반 HTTP 클라이언트를 구현하고, JWT Access/Refresh Token 관리(저장·갱신·인터셉터)를 `doc/AUTH_DESIGN.md`에 맞게 적용한다.

**Input:**

- Step 1 완료(패키지 구조·설정)
- doc/API_SPEC.md, doc/AUTH_DESIGN.md
- Backend `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout` API

**Scope:**

- 포함: Dio 인스턴스·Interceptors, Access Token Bearer 헤더 주입, 401 시 Refresh Token으로 자동 갱신, **flutter_secure_storage** 기반 Refresh Token 저장(모바일은 쿠키 미사용), ErrorResponse 파싱·일관된 에러 처리
- 제외: OAuth2, 소셜 로그인

**Instructions:**

- Dio 클라이언트 싱글톤 또는 Provider 설정
- `Authorization: Bearer {accessToken}` 인터셉터 구현
- 401 응답 시 Refresh Token으로 `/api/auth/refresh` 호출 후 Access Token 갱신·재시도
- Refresh Token은 `flutter_secure_storage`에 저장(AUTH_DESIGN 2.2, RULE 6.1.6)
- `ErrorResponse`(code, message, fieldErrors) 파싱 및 공통 예외 처리
- Access Token 만료 시(15분) 자동 갱신 로직, 갱신 실패 시 로그인 화면 이동

**Output Format:**

- 코드: `lib/data/api/`, `lib/data/auth/` 등
- API 클라이언트, AuthRepository(또는 TokenService), 인터셉터

**Constraints:**

- RULE 1.1: 토큰·비밀번호 로그 출력 금지
- RULE 6.1.6: 모바일은 Secure Storage 사용
- RULE 6.1.4: Access Token 15분 이하 유효기간 인지

**Done When:**

- 로그인 API 호출 시 Access Token 수신·저장, 인증된 API 호출 시 Bearer 헤더 자동 주입, 401 시 Refresh로 갱신·재시도가 동작한다.

**Duration:** 5일

**RULE Reference:** 1.1, 6.1, 6.5

---

## Step 3 — 회원·인증 화면 (회원가입·로그인)

**Step Name:** 회원·인증 화면 (회원가입·로그인)

**Step Goal:** 회원가입·로그인 화면을 구현하고, doc/API_SPEC.md의 Members·Auth API와 연동하여 인증 플로우를 완성한다.

**Input:**

- Step 2 완료(API 클라이언트·토큰 관리)
- doc/API_SPEC.md 2(회원), 3(인증)
- doc/AUTH_DESIGN.md

**Scope:**

- 포함: 회원가입 폼(이메일·비밀번호·닉네임, 클라이언트 검증), 로그인 폼(이메일·비밀번호), API 연동(POST /api/members, POST /api/auth/login), 성공 시 토큰 저장·홈 이동, 에러 표시(ErrorResponse fieldErrors)
- 제외: OAuth2, 비밀번호 재설정

**Instructions:**

- 회원가입 화면: `MemberJoinRequest` 필드 검증(이메일 형식, 비밀번호 8자 이상 등)
- 로그인 화면: `LoginRequest` 전송, 성공 시 `LoginResponse`에서 accessToken 수신, Refresh Token은 Backend Set-Cookie 대신 **모바일용 별도 응답 필드 또는 refresh API** 확인 — Backend가 쿠키만 지원 시 모바일 전용 토큰 전달 방식 협의
- 로그인 성공 시 `flutter_secure_storage`에 Refresh Token 저장
- 에러 시 `ErrorResponse`의 `fieldErrors`를 폼 필드에 표시
- 인증 상태 관리(Provider/Riverpod/Bloc 등)로 로그인 여부에 따른 화면 분기

**Output Format:**

- 화면: `lib/presentation/screens/auth/` (join_screen, login_screen)
- 위젯: 입력 폼·에러 표시·로딩 상태

**Constraints:**

- RULE 1.3: 클라이언트 검증만 믿지 말고, 서버 응답 에러도 표시
- RULE 1.2.2: 401·403 시 명확한 메시지

**Done When:**

- 회원가입·로그인 폼이 API와 연동되어 동작하고, 성공 시 메인 화면으로 이동, 실패 시 에러 메시지가 표시된다.

**Duration:** 4일

**RULE Reference:** 1.2, 1.3

---

## Step 4 — 공통 UI·네비게이션·라우팅

**Step Name:** 공통 UI·네비게이션·라우팅

**Step Goal:** 앱 전체 공통 UI(앱바·테마·로딩·에러 위젯)와 네비게이션·라우팅 구조를 확정하여 일관된 UX를 제공한다.

**Input:**

- Step 3 완료(인증 상태 관리)
- PRD 3(핵심 기능)

**Scope:**

- 포함: Material/Cupertino 테마, 로딩 인디케이터·에러 스낵바 등 공통 위젯, BottomNavigationBar 또는 Drawer 기반 네비게이션(지도·게시글·마이페이지 등), GoRouter/Navigator 2.0 라우팅, 인증/비인증 라우트 가드
- 제외: 개별 화면 상세 UI

**Instructions:**

- 테마(색상·Typography) 정의
- 공통 `LoadingWidget`, `ErrorWidget`, `EmptyWidget` 등
- 메인 탭: 지도(메인), 게시글 목록, 마이페이지
- 라우팅: `/`, `/login`, `/posts`, `/posts/:id`, `/me` 등
- 인증 필요 화면 진입 시 미로그인 시 로그인 화면으로 리다이렉트

**Output Format:**

- `lib/presentation/widgets/common/`
- `lib/presentation/navigation/` 또는 `lib/core/router/`
- `lib/main.dart` 앱 라우팅 설정

**Constraints:**

- RULE 1.2: 보호 화면은 인증 가드 적용

**Done When:**

- 메인 탭 네비게이션·라우팅이 동작하고, 인증 가드 및 공통 위젯이 적용된다.

**Duration:** 4일

**RULE Reference:** 1.2

---

## Step 5 — 게시글 목록·상세 화면

**Step Name:** 게시글 목록·상세 화면

**Step Goal:** 게시글(Post)·이미지 게시글(ImagePost) 목록·상세 화면을 구현하고, doc/API_SPEC.md의 Posts·ImagePosts API와 연동한다.

**Input:**

- Step 4 완료(네비게이션·공통 UI)
- doc/API_SPEC.md 4(게시글), 5(이미지 게시글)

**Scope:**

- 포함: 게시글 목록(GET /api/posts, 페이징·검색), 게시글 상세(GET /api/posts/{id}), 이미지 게시글 목록·상세(GET /api/image-posts, /api/image-posts/{id}), 상세 화면에서 위치(위도·경도)가 있으면 지도 미리보기 또는 지도 화면 이동
- 제외: 게시글 작성·수정·삭제, 반경 조회

**Instructions:**

- PostResponse·ImagePostResponse 모델 파싱
- 목록: `Page<T>` 구조(content, totalElements, totalPages 등) 처리, 무한 스크롤 또는 페이지네이션
- 상세: 제목·내용·이미지·작성자·위치 정보 표시
- 위치가 있는 게시글: 상세에서 지도 버튼 또는 인라인 미니맵(Step 6 연계)

**Output Format:**

- `lib/presentation/screens/posts/` (list, detail)
- `lib/presentation/screens/image_posts/` (list, detail)

**Constraints:**

- RULE 1.2: 비로그인도 목록·상세 조회 가능(API 스펙 준수)

**Done When:**

- 게시글·이미지 게시글 목록·상세가 API와 연동되어 표시되고, 페이징이 동작한다.

**Duration:** 4일

**RULE Reference:** 1.2

---

## Step 6 — 지도·위치·지도 SDK 연동

**Step Name:** 지도·위치·지도 SDK 연동

**Step Goal:** 지도 SDK(Google Maps / Kakao / Naver 등)를 연동하고, 사용자 현재 위치(GPS) 수집·표시 및 지도 중심 이동을 구현한다.

**Input:**

- Step 4 완료(네비게이션)
- doc/MAP_API.md, PRD 3.3(지도 기반 핵심 기능)
- Android/iOS 위치 권한 설정

**Scope:**

- 포함: 지도 SDK 플러그인(google_maps_flutter 등) 연동, 사용자 현재 위치(geolocator) 획득, 지도 중심을 사용자 위치로 이동, 마커·카메라 제어, 위치 권한 요청·거부 처리
- 제외: Pin 마커 표시, 반경 조회 UI(Step 7)

**Instructions:**

- `google_maps_flutter` 또는 `kakao_map_flutter` 등 프로젝트 정책에 맞는 SDK 선택
- Android: `AndroidManifest.xml` 위치 권한, iOS: `Info.plist` 권한 설명
- `Geolocator`로 현재 위치 획득, 지도 카메라 `animateCamera`로 해당 위치로 이동
- 위치 권한 거부 시 안내 메시지·설정 화면 이동 안내
- RULE 1.1: 지도 API Key는 환경 변수·dart-define으로 주입

**Output Format:**

- `lib/presentation/screens/map/` (map_screen)
- `lib/data/location/` (위치 서비스 래퍼)

**Constraints:**

- RULE 1.1: API Key 하드코딩 금지
- RULE 1.4.1: 위치 정보 로그 출력 시 정확도·민감도 고려

**Done When:**

- 지도가 표시되고, 사용자 현재 위치로 지도 중심이 이동하며, 권한 처리와 API Key 주입이 환경 변수로 동작한다.

**Duration:** 6일

**RULE Reference:** 1.1, 1.4.1

---

## Step 7 — Pin·반경 조회 화면

**Step Name:** Pin·반경 조회 화면

**Step Goal:** 지도에 Pin 마커를 표시하고, 반경 내 Pin·게시글·이미지 게시글 조회 API와 연동하여 지도 기반 SNS 핵심 기능을 완성한다.

**Input:**

- Step 5·6 완료(게시글 화면·지도)
- doc/API_SPEC.md 6(Pin), 4.6·5.6·6.6(반경 내 조회)
- doc/MAP_API.md 5(반경 조회 API)

**Scope:**

- 포함: 반경 내 Pin 조회(GET /api/pins/nearby), 반경 내 게시글(GET /api/posts/nearby), 반경 내 이미지 게시글(GET /api/image-posts/nearby), 지도에 Pin 마커 표시, 마커 클릭 시 Pin 상세·연결된 게시글 목록(GET /api/pins/{id}/posts, /api/pins/{id}/image-posts), 거리·반경 조절 UI
- 제외: Pin 생성·수정·삭제(Step 8에서 로그인 후)

**Instructions:**

- `lat`, `lng`, `radiusKm` 파라미터로 반경 API 호출(현재 위치 또는 지도 중심 기준)
- 지도에 Pin 마커 배치, 클릭 시 인포윈도우 또는 하단 시트에 Pin 설명·연결된 게시글 목록
- 게시글/이미지 게시글 항목 클릭 시 상세 화면(Step 5)으로 이동
- 반경 슬라이더 또는 고정 옵션(1km, 5km, 10km)

**Output Format:**

- `lib/presentation/screens/map/` 확장 (Pin 마커·인포윈도우)
- `lib/data/api/` (nearby API 호출)

**Constraints:**

- RULE 1.2: 반경 조회 API는 비로그인 허용(API 스펙)

**Done When:**

- 지도에 반경 내 Pin이 마커로 표시되고, 마커 클릭 시 Pin 정보·연결 게시글 목록이 표시되며, 게시글 상세로 이동한다.

**Duration:** 6일

**RULE Reference:** 1.2

---

## Step 8 — 게시글·이미지 게시글 작성·수정·삭제

**Step Name:** 게시글·이미지 게시글 작성·수정·삭제

**Step Goal:** 로그인 사용자를 위한 게시글·이미지 게시글 작성·수정·삭제 화면을 구현하고, doc/API_SPEC.md의 Posts·ImagePosts API와 연동한다.

**Input:**

- Step 5·7 완료(게시글 화면·지도·Pin)
- doc/API_SPEC.md 4.3·4.4·4.5, 5.3·5.4·5.5

**Scope:**

- 포함: 일반 게시글 작성(POST /api/posts, PostCreateRequest), 수정(PUT /api/posts/{id}), 삭제(DELETE /api/posts/{id}), 이미지 게시글 작성(multipart/form-data, image 파일 업로드), 수정·삭제, 위치 선택(지도에서 선택 또는 Pin 연결), 소유권 검증(403 시 에러 표시)
- 제외: Pin 생성·수정(별도 화면 또는 확장)

**Instructions:**

- 작성 폼: 제목·내용, 이미지 게시글은 이미지 선택·미리보기
- 위치: 지도 화면에서 좌표 선택 또는 기존 Pin 선택( pinId )
- `multipart/form-data`로 이미지 업로드(Dio `FormData`)
- 수정: 본인 글만 수정 가능, 403 시 "권한이 없습니다" 표시
- 삭제: 확인 다이얼로그 후 DELETE

**Output Format:**

- `lib/presentation/screens/posts/` (create, edit)
- `lib/presentation/screens/image_posts/` (create, edit)

**Constraints:**

- RULE 1.2: 로그인 필수, 403 시 명확한 메시지
- RULE 1.3: 입력 검증(클라이언트 + 서버 에러 표시)

**Done When:**

- 게시글·이미지 게시글 작성·수정·삭제가 API와 연동되어 동작하고, 401·403 시 적절히 처리된다.

**Duration:** 6일

**RULE Reference:** 1.2, 1.3

---

## Step 9 — 마이페이지·개인정보

**Step Name:** 마이페이지·개인정보

**Step Goal:** 마이페이지(내 게시글·이미지 게시글·Pin 목록)와 개인정보 수정 화면을 구현하고, doc/API_SPEC.md의 Me API와 연동한다.

**Input:**

- Step 4·8 완료(네비게이션·게시글 CRUD)
- doc/API_SPEC.md 7(마이페이지)

**Scope:**

- 포함: 내 게시글 목록(GET /api/me/posts), 내 이미지 게시글(GET /api/me/image-posts), 내 Pin 목록(GET /api/me/pins), 개인정보 수정(PUT /api/me, nickname 등), 로그아웃(POST /api/auth/logout)
- 제외: 관리자 기능

**Instructions:**

- 마이페이지 탭: 내 게시글·이미지 게시글·Pin 목록 카드 또는 탭으로 구분
- 각 목록 클릭 시 상세 화면(Step 5)으로 이동
- 개인정보 수정: 닉네임 등 수정 폼, PUT /api/me 호출
- 로그아웃: POST /api/auth/logout 호출 후 로컬 토큰 삭제, 로그인 화면으로 이동

**Output Format:**

- `lib/presentation/screens/me/` (my_page, edit_profile)

**Constraints:**

- RULE 1.2: 로그인 필수 API

**Done When:**

- 마이페이지에서 내 게시글·이미지 게시글·Pin 목록이 표시되고, 개인정보 수정·로그아웃이 동작한다.

**Duration:** 4일

**RULE Reference:** 1.2

---

## Step 10 — About·테스트·배포 준비

**Step Name:** About·테스트·배포 준비

**Step Goal:** About 페이지를 구현하고, 위젯·통합 테스트와 린트·보안 점검을 수행하여 배포 가능한 상태로 마무리한다.

**Input:**

- Step 1~9 완료
- PRD 3.5(About), RULE 4.2(테스트 규칙)

**Scope:**

- 포함: About 화면(서비스 소개·지도 기반 SNS 컨셉·기술 스택), 위젯 테스트 키 화면·위젯, 통합 테스트(API Mock 또는 테스트 서버), `flutter analyze`·`flutter test` 통과, Android/iOS 빌드 설정·스토어 제출용 메타데이터
- 제외: 실제 스토어 배포

**Instructions:**

- About 화면: PRD 3.5 내용 반영
- 위젯 테스트: 주요 위젯 `WidgetTest` 작성
- API Mock 또는 `Mockito`/`Mocktail`로 Repository 테스트
- `analysis_options.yaml` 린트 규칙, `flutter analyze` 무오류
- Android `build.gradle.kts`·iOS `Info.plist` 버전·권한·메타데이터 정리
- RULE 1.1: 프로덕션 빌드 시 API Key·base URL 환경 변수 확인

**Output Format:**

- `lib/presentation/screens/about/` (about_screen)
- `test/` 위젯·유닛 테스트
- `doc/` 또는 `mobile/README.md` 빌드·배포 가이드

**Constraints:**

- RULE 4.2: 테스트 작성 시 Given-When-Then, Assert 패턴
- RULE 1.1: 비밀정보 배포 패키지에 포함 금지

**Done When:**

- About 화면이 구현되고, `flutter analyze`·`flutter test` 통과, Android/iOS 빌드가 성공하며, 배포 가이드가 문서화된다.

**Duration:** 6일

**RULE Reference:** 1.1, 4.2, 4.3.2

---

> **문서 버전**: 1.0.0
> **최종 업데이트**: 2026-02-09
