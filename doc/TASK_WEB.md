# 지도 기반 SNS — React/TypeScript 웹 프론트엔드 TASK

> **기준 문서**: `doc/PRD.md`, `doc/RULE.md`, `doc/TASK_SERVER.md` (백엔드·API 연동 기준)
> **전제**: TASK_SERVER.md Step 1~11(백엔드 API) 완료 또는 동시 진행
> **총 Step**: 10단계

---

## 기술 스택 (React/TypeScript)

| 구분        | 기술                                                              |
| ----------- | ----------------------------------------------------------------- |
| 프레임워크  | React 19.x                                                        |
| 언어        | TypeScript 5.7.x                                                  |
| 빌드        | Vite 7.x                                                          |
| 스타일      | Tailwind CSS 4.x                                                  |
| 상태·데이터 | Zustand, TanStack React Query                                     |
| 라우팅      | React Router 7.x                                                  |
| HTTP        | Axios                                                             |
| 지도        | Kakao Map / Naver Map / Google Map 중 **추상화 설계** (교체 가능) |

---

## Step 템플릿 (RULE 4.3.2)

| 필드               | 설명                                    |
| ------------------ | --------------------------------------- |
| **Step Name**      | 단계 이름                               |
| **Step Goal**      | 단계 완료 시 달성할 목표(한 문장)       |
| **Input**          | 이 단계에 필요한 입력(문서·코드·API 등) |
| **Scope**          | 포함/제외 범위                          |
| **Instructions**   | 수행할 작업 목록                        |
| **Output Format**  | 산출물 형태·위치·형식                   |
| **Constraints**    | 반드시 지켜야 할 제약(RULE·기술 등)     |
| **Done When**      | 완료로 간주하는 조건                    |
| **Duration**       | 예상 소요 일수                          |
| **RULE Reference** | 참조 RULE.md 섹션                       |

---

## 일정 요약

| Phase       | Step 범위   | 기간(일) | 비고                      |
| ----------- | ----------- | -------- | ------------------------- |
| 기반        | Step 1 ~ 2  | 8        | 프로젝트 셋업·인증·라우팅 |
| 게시판      | Step 3      | 5        | 게시글·이미지 게시글 UI   |
| 지도        | Step 4 ~ 5  | 10       | 지도·Pin·연동             |
| 마이·관리자 | Step 6 ~ 8  | 12       | 마이페이지·관리자 UI      |
| 마무리      | Step 9 ~ 10 | 6        | 보안·배포                 |
| **합계**    | **10**      | **41**   |                           |

---

## Step 1 — React/TypeScript 프로젝트 셋업·아키텍처

**Step Name:** React/TypeScript 프로젝트 셋업·아키텍처

**Step Goal:** Vite·React·TypeScript 기반 웹 프로젝트 구조를 확정하고, API 연동·상태 관리·라우팅 아키텍처를 설계한다.

**Input:**

- PRD 2.2(Frontend 기술 스택), RULE 3.6(React·TypeScript 버전 정책)
- Backend API 명세(doc/API_SPEC.md 또는 Swagger)

**Scope:**

- 포함: Vite·React·TypeScript 초기화, 디렉터리 구조, 환경 변수(.env), Axios 인스턴스·베이스 URL, React Query·Zustand 골격, 라우팅 골격
- 제외: 실제 API 호출·비즈니스 화면 구현

**Instructions:**

- `web/` 또는 `frontend/` 디렉터리에 Vite + React + TypeScript 프로젝트 생성
- 디렉터리 구조: `src/pages/`, `src/components/`, `src/hooks/`, `src/api/`, `src/store/`, `src/types/`, `src/utils/`
- 환경 변수: `VITE_API_BASE_URL` 등 API 베이스 URL 분리
- Axios 인스턴스: 인터셉터(요청 시 Authorization 헤더·토큰 갱신), 타임아웃 설정
- React Query(`@tanstack/react-query`) 설정: QueryClient, 기본 staleTime 등
- Zustand: 인증 상태(accessToken, user) 골격
- React Router: `/`, `/login`, `/signup` 등 기본 경로 정의
- **RULE 3.6.6**: `package.json` exact 또는 tilde 버전 사용, `^` 신중히

**Output Format:**

- 코드: `web/src/` 또는 `frontend/src/` 하위 구조
- 설정: `vite.config.ts`, `tsconfig.json`, `.env.example`
- 문서: `doc/` 내 프론트엔드 아키텍처 요약(선택)

**Constraints:**

- RULE 3.6(React 19.x, TypeScript 5.6~5.7), 1.1(API Key·비밀정보 `.env` 외부 주입)

**Done When:**

- `npm run dev`로 개발 서버 기동, `npm run build` 성공, API 베이스 URL이 환경 변수로 주입된다.

**Duration:** 3일

**RULE Reference:** 3.6, 1.1, 4.4(주석)

---

## Step 2 — 인증·라우팅 (로그인·가입·JWT·보호 라우트)

**Step Name:** 인증·라우팅 (로그인·가입·JWT·보호 라우트)

**Step Goal:** 로그인·회원가입 UI를 구현하고, JWT(Access·Refresh Token) 기반 인증·보호 라우트를 적용한다.

**Input:**

- Step 1(프로젝트 구조), Backend API: POST `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`, POST `/api/members`
- RULE 6.1~6.5(JWT), 1.5.6(토큰 저장)

**Scope:**

- 포함: 로그인 폼·회원가입 폼, JWT 저장(HttpOnly Cookie 권장 또는 Secure Storage), Axios 인터셉터(401 시 Refresh), 보호 라우트(PrivateRoute), 로그아웃
- 제외: OAuth2, 비밀번호 찾기

**Instructions:**

- 로그인: 이메일·비밀번호 폼, POST `/api/auth/login`, 응답 토큰·사용자 정보 저장
- 회원가입: 이메일·비밀번호·닉네임 폼, POST `/api/members`, @Valid 검증 오류 표시
- **토큰 저장 (RULE 1.5.6)**: HttpOnly·Secure·SameSite Cookie 권장, localStorage에 Access Token만 저장 시 XSS 노출 인지
- Axios 인터셉터: 401 시 Refresh Token으로 갱신 시도, 실패 시 로그인 페이지 리다이렉트
- 보호 라우트: 미인증 시 `/login` 리다이렉트
- Zustand 또는 Context: `user`, `isAuthenticated` 상태
- **XSS 방지 (RULE 1.5.6)**: 사용자 입력 `dangerouslySetInnerHTML` 금지, 이스케이프 기본

**Output Format:**

- 페이지: `LoginPage`, `SignupPage`
- 컴포넌트: `PrivateRoute`, `AuthProvider` 또는 `useAuth` 훅
- API: `api/auth.ts`, `api/members.ts`

**Constraints:**

- RULE 1.2(401·403 명확), 1.3(입력 검증), 1.5.6(XSS·토큰 저장)

**Done When:**

- 로그인·가입이 동작하고, 미인증 시 보호 경로 접근이 `/login`으로 리다이렉트되며, 토큰 갱신·로그아웃이 정상 동작한다.

**Duration:** 5일

**RULE Reference:** 1.2, 1.3, 1.5.6, 6.1~6.5

---

## Step 3 — 게시글·이미지 게시글 UI

**Step Name:** 게시글·이미지 게시글 UI

**Step Goal:** 게시글(Post)·이미지 게시글(ImagePost) 목록·상세·작성·수정·삭제 화면을 React로 구현하고, Backend REST API와 연동한다.

**Input:**

- Step 2(인증), Backend API: GET/POST/PUT/DELETE `/api/posts`, `/api/image-posts`, `/api/image-posts/{id}/image`
- PRD 3.2(게시판 기능)

**Scope:**

- 포함: 게시글 목록(페이징·검색·공지 상단), 상세, 작성 폼(제목·내용·위치 선택), 수정·삭제(작성자만), 이미지 게시글 Multipart 업로드
- 제외: 지도 Pin 선택 UI(Step 5에서 연동)

**Instructions:**

- 목록: GET `/api/posts`, `/api/image-posts` (keyword, page, size), React Query `useQuery`, 무한 스크롤 또는 페이지네이션
- 상세: GET `/api/posts/{id}`, `/api/image-posts/{id}`, 이미지 URL `/api/image-posts/{id}/image`
- 작성: POST, 로그인 필수, FormData(이미지 게시글), 위치(위도·경도) 선택 UI 골격
- 수정·삭제: 작성자만 버튼 노출, 403 처리
- **XSS 방지**: 제목·내용 등 사용자 입력은 React 기본 이스케이프, `dangerouslySetInnerHTML` 금지

**Output Format:**

- 페이지: `PostListPage`, `PostDetailPage`, `PostCreatePage`, `PostEditPage`, `ImagePostListPage`, `ImagePostDetailPage`, `ImagePostCreatePage`, `ImagePostEditPage`
- API: `api/posts.ts`, `api/image-posts.ts`

**Constraints:**

- RULE 1.2(IDOR·403), 1.3(클라이언트 검증만 믿지 않기), 1.5.6(XSS)

**Done When:**

- 게시글·이미지 게시글 목록·상세·작성·수정·삭제가 API와 연동되어 동작한다.

**Duration:** 5일

**RULE Reference:** 1.2, 1.3, 1.5.6

---

## Step 4 — 지도·Pin UI

**Step Name:** 지도·Pin UI

**Step Goal:** 메인 지도 화면을 구현하고, 사용자 위치·반경 내 Pin을 표시하며, Pin 클릭 시 관련 게시글 목록·상세로 이동한다.

**Input:**

- Step 2(인증), Backend API: GET `/api/pins/nearby`, GET `/api/pins/{id}/posts`, `/api/pins/{id}/image-posts`
- PRD 3.3(지도 핵심 기능)

**Scope:**

- 포함: 지도 메인 페이지, GPS/위치 권한·현재 위치 표시, 반경 내 Pin 마커, Pin 클릭 시 게시글 목록·상세 링크
- 제외: 경로 시각화, Pin 생성·수정 UI(Step 5 연동)

**Instructions:**

- 지도 SDK: Kakao/Naver/Google 중 1개 선택, 환경 변수로 API Key 주입
- 사용자 위치: `navigator.geolocation`, 권한 거부 시 폴백 처리
- 반경 내 Pin: GET `/api/pins/nearby?lat=&lng=&radiusKm=` 연동
- Pin 마커: 지도에 표시, 클릭 시 해당 Pin의 게시글 목록 페이지(`/pins/{id}/posts`)로 이동
- 지도·Pin 표시는 비로그인 조회 가능(API 허용 시)

**Output Format:**

- 페이지: `MapPage`, `PinPostsPage`(Pin별 게시글 목록)
- 컴포넌트: `MapView`, `PinMarker`
- API: `api/pins.ts`

**Constraints:**

- RULE 1.5.6(XSS), 1.3(클라이언트 검증만 믿지 않기)

**Done When:**

- 메인 지도에서 현재 위치와 반경 내 Pin이 표시되고, Pin 클릭 시 관련 게시글 목록·상세로 이동한다.

**Duration:** 6일

**RULE Reference:** 1.5.6, 1.3

---

## Step 5 — 지도-게시글 연동·경로/거리

**Step Name:** 지도-게시글 연동·경로/거리

**Step Goal:** 게시글 작성 시 위치/Pin 연결, 게시글 상세에 지도 위치 표시, 사용자 위치→목적지 거리·경로 시각화를 구현한다.

**Input:**

- Step 3·4(게시글·지도 UI), Backend API: Post·ImagePost 위치 필드, Pin CRUD, 거리/경로 API(있는 경우)
- PRD 3.3(경로·거리)

**Scope:**

- 포함: 글 작성 시 위치(위도·경도) 또는 Pin 선택, 상세 페이지 지도에 위치 표시, 거리 계산·경로 시각화(출발/도착)
- 제외: 실시간 경로 추적

**Instructions:**

- 게시글 작성 폼: 지도 연동 위치 선택 또는 기존 Pin 선택
- 게시글 상세: 지도에 해당 위치·Pin 표시
- 거리·경로: 사용자 위치→선택 위치 거리( Haversine 또는 API), (선택) 경로 폴리라인
- 출발/도착 지점 설정 UI

**Output Format:**

- 컴포넌트: `LocationPicker`, `MapWithLocation`, `DistanceDisplay`
- 게시글 작성·상세 화면에 지도·위치·경로 연동

**Constraints:**

- RULE 2.1(API 책임 단위 유지)

**Done When:**

- 글 작성 시 위치/Pin이 연결되고, 상세에서 지도에 표시되며, 거리·경로(출발/도착)가 표시된다.

**Duration:** 4일

**RULE Reference:** 2.1

---

## Step 6 — 마이페이지·About 페이지

**Step Name:** 마이페이지·About 페이지

**Step Goal:** 내 게시글·이미지 게시글·Pin 목록, 개인정보 수정, About 페이지를 제공한다.

**Input:**

- Step 2(인증), Backend API: GET `/api/me/posts`, `/api/me/image-posts`, `/api/me/pins`, PUT `/api/me`
- PRD 3.4, 3.5

**Scope:**

- 포함: 마이페이지(내 글·Pin 목록, 페이징), 개인정보 수정(닉네임 등), About(서비스 소개·기술 스택)
- 제외: 프로필 이미지·활동 통계

**Instructions:**

- 마이페이지: 로그인 사용자 전용, GET `/api/me/*` 연동
- 개인정보 수정: 본인만, PUT `/api/me`
- About: 정적 페이지, 서비스 소개·지도 SNS 컨셉·기술 스택

**Output Format:**

- 페이지: `MyPage`, `MeEditPage`, `AboutPage`
- API: `api/me.ts`

**Constraints:**

- RULE 1.2(리소스 소유권 검증)

**Done When:**

- 로그인 사용자가 자신의 글·Pin 목록을 보고, 개인정보를 수정할 수 있으며, About 페이지가 노출된다.

**Duration:** 4일

**RULE Reference:** 1.2

---

## Step 7 — 관리자 회원·게시물 관리 UI

**Step Name:** 관리자 회원·게시물 관리 UI

**Step Goal:** ROLE_ADMIN 전용으로 회원 목록·추가·수정·삭제, 게시글·이미지 게시글 목록·수정·삭제·공지 등록/해제 화면을 제공한다.

**Input:**

- Step 2(인증·ROLE_ADMIN), Backend API: `/api/admin/members`, `/api/admin/posts`, `/api/admin/image-posts`
- PRD 3.6.1, 3.6.2

**Scope:**

- 포함: 회원 목록(페이징·검색)·추가·수정·삭제, 게시글·이미지 게시글 목록·수정·삭제·공지 등록/해제
- 제외: 대량 삭제·역할 일괄 변경

**Instructions:**

- 관리자 라우트: `/admin/*` 접근 시 ROLE_ADMIN 검증, 미권한 시 403·리다이렉트
- 회원 관리: GET/POST/PUT/DELETE `/api/admin/members` 연동
- 게시물 관리: GET/PUT/DELETE `/api/admin/posts`, `/api/admin/image-posts`, PATCH `/{id}/notice` 연동
- 공지 상단 노출은 API 정렬에 의존

**Output Format:**

- 페이지: `AdminMemberListPage`, `AdminMemberFormPage`, `AdminPostListPage`, `AdminImagePostListPage`, `AdminPostEditPage` 등
- API: `api/admin.ts` 또는 `api/admin/members.ts`, `api/admin/posts.ts`

**Constraints:**

- RULE 1.2(최소 권한, 관리자만 접근), PRD 3.6

**Done When:**

- 관리자만 회원·게시물 관리에 접근 가능하고, 일반 사용자는 403이다.

**Duration:** 6일

**RULE Reference:** 1.2, PRD 3.6

---

## Step 8 — 관리자 통계 UI

**Step Name:** 관리자 통계 UI

**Step Goal:** 회원 가입·로그인·글 통계(기간별)를 API·대시보드(테이블·차트)로 제공한다.

**Input:**

- Step 7(관리자 UI), Backend API: GET `/api/admin/stats/signup`, `/admin/stats/login`, `/admin/stats/posts`
- PRD 3.6.3, 3.6.4

**Scope:**

- 포함: 가입·로그인·글 통계 API 연동, 기간 필터·테이블·차트(Recharts 등)
- 제외: 실시간 대시보드

**Instructions:**

- 통계 API 연동: 기간 파라미터, 응답 타입 정의
- 테이블·차트: Recharts 또는 선택 라이브러리
- 기간 선택 UI: 일/주/월/분기/년

**Output Format:**

- 페이지: `AdminStatsPage`
- API: `api/admin/stats.ts`
- 컴포넌트: `StatsChart`, `StatsTable`

**Constraints:**

- RULE 2.3(읽기 전용), 1.2(관리자만)

**Done When:**

- 관리자가 기간을 선택해 가입·로그인·글 통계를 조회하고, 차트로 확인할 수 있다.

**Duration:** 4일

**RULE Reference:** 2.3, 1.2

---

## Step 9 — 보안·CORS·에러 처리 점검

**Step Name:** 보안·CORS·에러 처리 점검

**Step Goal:** RULE 기준으로 클라이언트 보안을 점검하고, CORS·에러 처리·토큰 저장 방식을 검증한다.

**Input:**

- RULE 1(보안), Step 1~8 완료 코드

**Scope:**

- 포함: CORS 허용 오리진 확인(Backend 연동), 401/403 에러 처리·리다이렉트, 토큰 저장 방식 검증, XSS·CSRF 대응 확인
- 제외: Rate Limiting(Backend 담당)

**Instructions:**

- CORS: Backend allow-list에 프론트엔드 오리진 포함 확인
- 401/403: Axios 인터셉터·공통 에러 핸들링
- 토큰: HttpOnly Cookie 또는 Secure Storage 사용 검증
- XSS: `dangerouslySetInnerHTML` 미사용, 사용자 입력 이스케이프 확인
- 에러 메시지: 스택 트레이스·내부 경로 노출 금지

**Output Format:**

- 점검 결과: 체크리스트 또는 간단 보고

**Constraints:**

- RULE 1.2.3(CORS), 1.5.6(XSS·토큰), 1.6(보안 설정)

**Done When:**

- CORS·에러 처리·토큰 저장이 RULE을 만족한다고 확인된다.

**Duration:** 2일

**RULE Reference:** 1.2.3, 1.5.6, 1.6

---

## Step 10 — 빌드·배포·최종 점검

**Step Name:** 빌드·배포·최종 점검

**Step Goal:** 프로덕션 빌드·배포 가이드를 완성하고, Backend API·환경 변수 연동을 최종 점검한다.

**Input:**

- Step 1~9 완료, Backend 배포 설정

**Scope:**

- 포함: `npm run build` 최적화, 환경 변수(.env.production), 정적 리소스 배포 경로, Backend 프록시 또는 CORS 설정
- 제외: CI/CD 파이프라인 구현

**Instructions:**

- 프로덕션 빌드: `vite build`, 청크 분할·압축 확인
- 환경 변수: `VITE_API_BASE_URL` 등 `.env.production` 정의
- 배포: Nginx·CDN 또는 Backend 정적 서빙 구조 문서화
- Backend 연동: API 베이스 URL·CORS 최종 확인

**Output Format:**

- 설정: `vite.config.ts` 프로덕션 최적화
- 문서: `doc/` 내 배포 가이드

**Constraints:**

- RULE 1.1(비밀정보 외부 주입), PRD 7(운영 기준)

**Done When:**

- 프로덕션 빌드가 성공하고, 배포 가이드가 준비되었으며, Backend API 연동이 정상 동작한다.

**Duration:** 4일

**RULE Reference:** 1.1, PRD 7

---

## RULE 참조 요약

| 영역 | 참조                                              |
| ---- | ------------------------------------------------- |
| 보안 | RULE 1 (비밀정보, 인증·인가, 입력검증, XSS, CORS) |
| 기능 | RULE 2 (API 설계, 2.1.3 Admin API 분리)           |
| 기술 | RULE 3.6 (React·TypeScript 버전)                  |
| 품질 | RULE 4 (주석, 문서)                               |

---

## 진행 추적 (체크리스트)

| Step | 내용                                    | 완료 |
| ---- | --------------------------------------- | ---- |
| 1    | React/TypeScript 프로젝트 셋업·아키텍처 | ☑    |
| 2    | 인증·라우팅 (로그인·가입·JWT)           | ☑    |
| 3    | 게시글·이미지 게시글 UI                 | ☐    |
| 4    | 지도·Pin UI                             | ☐    |
| 5    | 지도-게시글 연동·경로/거리              | ☐    |
| 6    | 마이페이지·About                        | ☐    |
| 7    | 관리자 회원·게시물 관리 UI              | ☐    |
| 8    | 관리자 통계 UI                          | ☐    |
| 9    | 보안·CORS·에러 처리 점검                | ☐    |
| 10   | 빌드·배포·최종 점검                     | ☐    |

---

> **문서 버전**: 1.0.0
> **기준**: PRD 1.0.0, RULE 1.0.8, TASK_SERVER.md (백엔드)
> **최종 업데이트**: 2026-02-09
> **비고**: TASK_SERVER.md(Thymeleaf)는 별도 유지, 수정·삭제하지 않음
