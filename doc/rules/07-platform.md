# 7. 플랫폼별 구현 가이드 (Platform-specific Implementation)

> 원본: [RULE.md](../RULE.md) 7장. React 세부 규칙은 [08-javascript.md](08-javascript.md)와 함께 참조.

### 7.1 Spring Boot (Back-end)

백엔드에서는 **보안 규정**과 **데이터 무결성**에 집중한다.

#### 7.1.1 [보안] 환경 변수 관리

**Rule**: DB 비밀번호 등 민감 정보는 `application.yml`에 직접 노출하지 않고 **환경 변수**를 사용한다.

```yaml
# Good Case
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

#### 7.1.2 [기능] 전역 에러 핸들러 (Global Exception Handler)

**Rule**: 모든 예외는 **공통된 객체 형식**으로 반환하여 클라이언트의 처리를 돕는다.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse response = new ErrorResponse(e.getErrorCode(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
```

#### 7.1.3 [API] URI 설계 및 계층형 자원 구현 (RULE 2.1.4)

**Rule**: 계층형 자원 지향 URI를 적용하고, `@Controller`,`@RestController`에서 `@RequestMapping`·`@PathVariable`로 계층을 표현한다.

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 1. 컬렉션
    @GetMapping
    public Page<OrderResponse> getOrders(@ModelAttribute OrderSearchCond cond, Pageable pageable) {
        return orderService.search(cond, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderCreateRequest req) {
        return orderService.create(req);
    }

    // 2. 단일 리소스
    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrder(@PathVariable Long orderId) {
        return orderService.getDetail(orderId);
    }

    @PatchMapping("/{orderId}")
    public OrderResponse updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateRequest req) {
        return orderService.update(orderId, req);
    }

    // 3. 하위 리소스
    @GetMapping("/{orderId}/items")
    public List<OrderItemResponse> getOrderItems(@PathVariable Long orderId) {
        return orderService.getItems(orderId);
    }

    @PostMapping("/{orderId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItemResponse addOrderItem(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemCreateRequest req) {
        return orderService.addItem(orderId, req);
    }

    // 4. 액션
    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long orderId) {
        return orderService.cancel(orderId);
    }
}
```

**표준 URI 구조 예시** (2026년 기준):

```text
/api/v{version}                     ← v1 생략 가능
  /orders                           ← 컬렉션
  /orders/{orderId}
  /orders/{orderId}/items
  /orders/{orderId}/items/{itemId}
  /orders/{orderId}/cancel          ← 상태 변경 액션
  /orders/search                    ← 검색 전용
  /me/orders                        ← 현재 사용자 관련
```

---

### 7.2 React (Web Front-end)

프론트엔드에서는 **입력값 검증**과 **인증 토큰 관리**가 핵심이다.

#### 7.2.1 [보안] XSS 방지 및 상태 관리

**Rule**: 사용자 입력값을 렌더링할 때 `dangerouslySetInnerHTML` 사용을 **금지**하며, API 통신 시 토큰은 **메모리**나 **HttpOnly 쿠키**에 저장한다.

#### 7.2.2 [기술 제한] API 인스턴스 공통화

**Rule**: 모든 API 호출은 **공통 Axios 인스턴스**를 사용하며, 헤더에 토큰을 자동으로 주입한다.

#### 7.2.3 폴더 구조 (Feature-based + Atomic Design)

**Rule**: Feature-based 구조를 기본으로 하고, 공통 컴포넌트는 Atomic Design 계층(Atoms, Molecules, Organisms)을 적용한다.

#### 7.2.4 컴포넌트·타입 구성 규칙

**Rule**: Functional Component + Hooks만 사용(Class Component 금지), Props는 interface로 명시, 컴포넌트당 150줄 이하, 단일 책임 원칙(SRP).

#### 7.2.5 JSX-TSX 구조 레벨 제한

| Rule                | 설명                                            |
| ------------------- | ----------------------------------------------- |
| **Depth 제한**      | JSX 중첩 3~4단계 초과 시 새 컴포넌트로 분리     |
| **Early return**    | if/else 중첩 금지, 조건부 렌더링은 early return |
| **Fragment**        | 불필요한 div 금지, `<>` 또는 `<Fragment>` 사용  |
| **Props spreading** | `{...props}` 금지, 명시적 prop 전달             |
| **Children prop**   | `children`은 마지막 prop으로 배치               |

#### 7.2.6 스타일링 방식

**Rule**: Tailwind + clsx + tailwind-merge 패턴 권장. `cn()` 유틸로 조건부 클래스 합성.

#### 7.2.7 ESLint·Prettier 설정

**Rule**: ESLint + Prettier 설정 시 `prettier`는 `extends` 마지막에 배치, React 17+ JSX transform·TypeScript 사용 시 `react/prop-types` off.

#### 7.2.8 컴포넌트 export 순서

파일 하단: `export { default }` → `export type { Props }` → `export * from './types'` (필요 시).

---

### 7.3 Flutter (Mobile App)

> **7.2 React**와 **8장 JS 규칙**과 **동일한 원칙**(보안 저장, 비동기·에러 처리, 네이밍 등)을 적용한다. 아래는 Flutter **특화 사항**만 기술한다. 상태 관리(Riverpod/Bloc), 폴더 구조, 테스트 규칙 등은 팀에서 7.2 수준의 가이드를 별도로 두는 것을 권장한다.

#### 7.3.1 [보안] 보안 저장소 사용

**Rule**: 민감한 데이터(자동 로그인 토큰 등)는 `SharedPreferences`가 아닌 **`flutter_secure_storage`**를 사용한다.

#### 7.3.2 [기술 제한] 에러 바운더리 및 로깅

**Rule**: 비동기 작업 시 반드시 **try-catch**를 사용하며, UI 레벨에서 사용자에게 에러 상황을 알린다.
