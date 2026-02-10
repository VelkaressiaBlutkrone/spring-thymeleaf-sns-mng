# 8. 프론트엔드 JavaScript 코딩 규칙 (2026 ver.)

> **7.2 React**의 하위 세부 규칙이다. React·Vue·Next.js·Vite 등 프론트엔드 코드 작성 시 7.2와 본 장(8)을 함께 참조한다. 원본: [RULE.md](../RULE.md) 8장.

### 8.1 변수 & 상수 선언 규칙 (Variables & Constants)

| #   | Rule                                                                                                                                                          | 비고                                            |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------- |
| 1   | **var는 절대 사용하지 않는다** (ES6 이후 완전 금지)                                                                                                           |                                                 |
| 2   | 재할당이 필요 없는 모든 변수는 **const**로 선언한다. (기본 원칙: const > let)                                                                                 |                                                 |
| 3   | 재할당이 반드시 필요한 경우에만 **let**을 사용한다. (for 루프의 i, j 등은 let 허용, for...of/forEach 내부에서는 const 추천)                                   |                                                 |
| 4   | const와 let은 **사용 직전에 선언**한다. (hoisting 문제 최소화)                                                                                                |                                                 |
| 5   | 같은 스코프 내에서 const를 let보다 위에 선언한다. (가독성)                                                                                                    |                                                 |
| 6   | **전역 변수는 절대 사용하지 않는다.** (window.xxx, globalThis.xxx 직접 할당 금지. 필요 시 Context, Zustand, Redux, Jotai 등 상태 관리 도구 사용)              |                                                 |
| 7   | **네이밍**: 변수/함수 → camelCase, 상수(진짜 바뀌지 않는 값) → UPPER*SNAKE_CASE, 컴포넌트 → PascalCase (React/Vue), private/internal 변수 → `*`로 시작 (옵션) | 예: `const MAX_UPLOAD_SIZE = 10 * 1024 * 1024;` |

### 8.2 함수 선언 및 사용 규칙 (Functions)

| #   | Rule                                                                                                                         | 비고 |
| --- | ---------------------------------------------------------------------------------------------------------------------------- | ---- |
| 1   | **네이밍**: camelCase, 동사 + 목적어 형태 권장. (좋음: fetchUserData, calculateTotalPrice, handleSubmit / 나쁨: fn, doIt, x) |      |
| 2   | **화살표 함수**를 기본으로 사용 (특히 콜백, 짧은 함수). `function name() {}` 은 생성자, this 바인딩 필요, 재귀 함수에만 사용 |      |
| 3   | **async 함수**는 반드시 async 키워드 명시                                                                                    |      |
| 4   | 함수는 **한 가지 역할만** 수행. 길이 30~40줄 이상이면 리팩토링 검토                                                          |      |
| 5   | 매개변수 기본값 적극 활용: `function greet(name = 'Guest') {}`                                                               |      |
| 6   | 반환은 early return 또는 단일 return 중 팀 컨벤션 따름                                                                       |      |
| 7   | 익명 함수는 거의 사용하지 않는다. (map, filter 등 짧은 화살표 함수는 예외 허용)                                              |      |

### 8.3 비동기 처리 규칙 (Async/Await 중심)

| #   | Rule                                                                                                                                        | 비고                                                       |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------- |
| 1   | **비동기 처리 기본 방식은 async/await**. `.then().catch()` 체인은 최대한 피한다. 콜백 스타일(callback hell)은 절대 사용하지 않는다.         |                                                            |
| 2   | 모든 비동기 함수는 `async`로 선언한다.                                                                                                      | `async function fetchUser(id) { ... }`                     |
| 3   | Promise를 반환하는 함수는 **await 없이 호출하지 않는다**. (fire-and-forget 금지)                                                            |                                                            |
| 4   | 병렬 비동기 처리 시 **Promise.all** 적극 활용                                                                                               | `const [user, posts, comments] = await Promise.all([...])` |
| 5   | **AbortController** 적극 활용 (React 18+ concurrent mode 대비). 타임아웃·취소 가능 fetch wrapper 사용 권장                                  |                                                            |
| 6   | 로딩·에러 상태 관리는 **React Query, SWR, TanStack Query** 등 라이브러리 사용을 기본으로 한다. 직접 구현 시 `useState` + `useEffect`로 관리 |                                                            |

### 8.4 예외 및 오류 처리 규칙 (Error Handling)

| #   | Rule                                                                                                                                                         | 비고 |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---- |
| 1   | **async 함수 내에서는 반드시 try-catch 사용**                                                                                                                |      |
| 2   | **커스텀 에러 클래스** 적극 활용 (도메인별 에러 구분. 예: AuthError, NetworkError)                                                                           |      |
| 3   | **최상위 레벨**에서 전역 에러 핸들링: React ErrorBoundary, Next.js `_error.tsx` 또는 `global-error.tsx`, Vue `app.config.errorHandler`                       |      |
| 4   | `console.log`는 **개발 환경에서만 허용**. 프로덕션에서는 Sentry, Datadog, LogRocket 등 에러 모니터링 도구 사용                                               |      |
| 5   | **Promise rejection은 반드시 catch 처리**. unhandledrejection 이벤트는 모니터링 도구에 연결                                                                  |      |
| 6   | 예상 가능한 에러(404, 401, 403 등) → 사용자 친화적 메시지로 변환. 예상치 못한 에러(500, 네트워크 오류) → "알 수 없는 오류가 발생했습니다" + 재시도 버튼 제공 |      |

### 8.5 네이밍 컨벤션 전체 요약

| 대상             | 규칙              | 예시                              |
| ---------------- | ----------------- | --------------------------------- |
| 변수·함수        | camelCase         | `fetchUserData`, `handleSubmit`   |
| 상수(불변)       | UPPER_SNAKE_CASE  | `MAX_UPLOAD_SIZE`, `API_BASE_URL` |
| 컴포넌트         | PascalCase        | `UserProfile`, `LoginForm`        |
| private/internal | `_`로 시작 (옵션) | `_internalCache`                  |

### 8.6 금지 패턴 (Do Not)

- ❌ **var** 사용
- ❌ **.then().catch()** 체인 남용 (async/await 우선)
- ❌ **전역 변수** (window.xxx, globalThis.xxx 직접 할당)
- ❌ **fire-and-forget** (Promise 반환 함수를 await 없이 호출)
- ❌ **콜백 지옥** (callback hell)
- ❌ **dangerouslySetInnerHTML** (XSS 위험, React)

### 8.7 추천 라이브러리 & 패턴

| 용도            | 추천                                           |
| --------------- | ---------------------------------------------- |
| 서버 상태·캐싱  | React Query, TanStack Query, SWR               |
| 폼·입력 검증    | Zod, React Hook Form, Yup                      |
| 에러 모니터링   | Sentry, Datadog, LogRocket                     |
| 상태 관리       | Zustand, Redux Toolkit, Jotai                  |
| HTTP 클라이언트 | Axios (인터셉터 활용), fetch + AbortController |
