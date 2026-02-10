# 3. 기술(Technical) 제한 RULE — 프로젝트 생존성 [SHOULD]

> 원본: [RULE.md](../RULE.md) 3장. 상세 AOP는 [aop-guide.md](aop-guide.md) 분리 권장.

### 3.1 아키텍처 계층 규칙

#### 3.1.1 계층 의존성

- 계층 간 의존성 **단방향 유지**

```text
Controller → Service → Domain/Repository
```

#### 3.1.2 금지

- ❌ Controller → Repository 직접 호출
- ❌ Domain이 Infra 기술(Spring, JPA 등)에 종속

### 3.2 프레임워크 의존성 제한

#### 3.2.1 의존성 제한

- 특정 프레임워크 기능에 과도한 종속 금지
- 프레임워크 교체 불가능한 구조 금지

#### 3.2.2 예시

- ❌ 비즈니스 로직이 `@Controller`, `@Entity`에 직접 묶여 있음

### 3.3 ORM 사용 규칙

#### 3.3.1 ORM 기본 원칙

- **N+1 문제 인지하고 대응**
- `EAGER` 기본 금지, **`LAZY` 기본**
- 엔티티를 API 응답 객체로 직접 반환 금지

#### 3.3.2 QueryDSL 개발 규칙 (QueryDSL 사용 시에만 적용)

> **적용 조건**: 프로젝트에서 **QueryDSL을 사용하는 경우**에만 아래 규칙을 적용한다. QueryDSL을 사용하지 않으면 해당 없다.

##### 3.3.2.1 JPAQueryFactory 주입

- `QueryDslConfig`에서 Bean으로 등록된 `JPAQueryFactory`를 사용한다.
- 서비스/레포지토리 레이어에서는 `@RequiredArgsConstructor`를 활용하여 주입한다.

##### 3.3.2.2 Q클래스 사용

- QueryDSL이 자동 생성한 Q클래스만을 사용한다.
  - 예시: `QReservation.reservation`, `QMember.member`
- 직접 Q객체를 생성해서 사용하지 않는다.

##### 3.3.2.3 페치 조인(fetchJoin) 활용

- N+1 문제 방지를 위해 반드시 `fetchJoin()`을 필요한 쿼리에 추가한다.

```java
queryFactory.selectFrom(reservation)
  .join(reservation.member, member).fetchJoin()
```

### 3.4 동기 / 비동기 통신 규칙

#### 3.4.1 통신 원칙

- 외부 시스템 호출은 **Timeout 필수**
- **Retry 정책 명시**
- Circuit Breaker 없는 무한 재시도 금지

### 3.5 AOP 개발 규칙 (Spring AOP 설계 표준)

> **AOP는 강력한 도구이지만, 남용되는 순간 시스템 복잡도를 폭증시킨다.** 보조적 역할로만 사용하고, 명시성 > 추상화, 예측 가능성 우선.
> 상세 AOP 가이드는 필요 시 **별도 문서(rules/aop-guide.md)** 로 분리하여 유지보수성을 높일 수 있다.

AOP는 **횡단 관심사(Cross-Cutting Concern)** 전용이며, 비즈니스 로직·도메인 규칙·상태 변경·핵심 흐름 제어에는 사용하지 않는다.

#### 3.5.1 허용 대상 — 횡단 관심사만

- **허용**: 트랜잭션 관리(`@Transactional`), 로깅·트레이싱, 보안·권한 체크, 성능 측정·메트릭, 캐싱, 예외 변환·알림
- **금지**: 핵심 비즈니스 규칙, 도메인 정책 분기, 상태 변경 로직

**Rule**: AOP 안에는 **"업무 규칙을 판단하는 코드"**가 절대 들어가면 안 된다.

**이유**: 흐름이 코드 외부에서 변경됨, 디버깅 불가능, 유지보수 비용 급증.

#### 3.5.2 보조 역할 — 필수 요소 금지

- ❌ **나쁜 예**: AOP가 없으면 서비스가 정상 동작하지 않음, AOP 내부에서 필수 데이터 세팅
- ✅ **좋은 예**: AOP 제거해도 시스템은 동작, 단지 "추가 정보"만 사라짐

**Rule**: **AOP는 제거해도 시스템이 정상 동작해야 한다.**

#### 3.5.3 Aspect 설계 — 단일 책임·이름 규칙

- **Aspect 1개 = 관심사 1개** (예: `LoggingAspect`, `SecurityAspect`, `MetricAspect`)
- **클래스명**: `~Aspect` 접미사 필수
- **패키지**: `aop`, `aspect` 하위 패키지 권장

```text
com.company.project.common.aop
 ├── LoggingAspect
 ├── SecurityAspect
 └── MetricAspect
```

**Rule**: **Aspect는 반드시 단일 책임을 가진다.** `CommonAspect`(로깅+권한+트랜잭션 혼합) 등 God Aspect 금지.

#### 3.5.4 Pointcut — 패키지 기반 기본, Annotation 제한적

- **기본**: 패키지 기반 Pointcut 사용 (구조 변경에 강함, 메서드 단위 오남용 방지)

```java
@Pointcut("execution(* com.company.project..service..*(..))")
```

- **Annotation 기반**: 의미가 명확하고 팀 내 공통 인식이 있을 때만 제한적 사용

```java
@Pointcut("@annotation(LogExecution)")
```

- ❌ **금지**: `execution(* com.app..*(..))` (패키지 전체), `execution(* *..*Service.save*(..))` (메서드 이름 기반)

**Rule**: **패키지 전체 포인트컷 금지. 패키지 기반 기본, Annotation은 제한적 사용. 메서드 이름 기반 Pointcut 금지.**

**이유**: 메서드 이름 기반은 리팩토링 취약, 의도 파악 어려움.

#### 3.5.5 Advice 사용 — 종류별 기준, @Around 남용 금지

| Advice          | 사용 기준                 |
| --------------- | ------------------------- |
| @Before         | 사전 검증, 권한 체크      |
| @AfterReturning | 결과 후처리, 로깅         |
| @AfterThrowing  | 예외 변환, 알림           |
| @Around         | 실행 제어가 필요한 경우만 |

**Rule**: **@Around는 반드시 필요한 경우에만 사용.** `proceed()` 누락 방지, try-catch 범위 최소화.

#### 3.5.6 예외 처리 — 삼키기 금지, 재throw

- ❌ **금지**: AOP 내부에서 예외 catch 후 `return null` 또는 예외 무시
- ✅ **허용**: 예외 로깅 후 **재throw** (`throw e`) 또는 명시적 변환

**Rule**: **AOP는 예외를 기록만 하고, 판단하거나 삼키지 않는다.** (Global Exception Handler가 담당)

#### 3.5.7 트랜잭션 — Service 계층만

- **허용 위치**: Application Service, UseCase Layer
- **금지 위치**: Domain Model, Repository 구현체, Controller

**Rule**: **`@Transactional`은 Service 계층에만 선언한다.**

**이유**: 트랜잭션 범위 추적 불가, Lazy Loading 오류, 테스트 어려움.

#### 3.5.8 상태 변경·무거운 작업 금지

- ❌ **금지**: Entity 수정, Request 객체 변경, ThreadLocal 값 임의 조작
- ❌ **금지**: AOP 내부에서 DB 접근, 외부 API 호출, 복잡한 계산 로직
- ✅ **허용**: 읽기 전용 접근, 별도 컨텍스트 객체에 기록

**Rule**: **AOP는 관찰자(observer) 역할만 수행한다.**

#### 3.5.9 순서(Order) 명시

- 다중 AOP 사용 시 **`@Order` 필수** (예: Security → Transaction → Logging)
- 하나의 메서드에 적용되는 Aspect는 **최대 2~3개 이내** 권장

**Rule**: **다중 AOP 사용 시 `@Order` 필수.** AOP 적용으로 실행 흐름 추론이 어려워지면 안 된다.

#### 3.5.10 성능 민감 영역 — 사전 검증

- **대상**: 대량 반복 호출, 배치, 스트리밍 처리

**Rule**: **반복 호출되는 로직에는 AOP 적용 전 성능 테스트 필수.**

#### 3.5.11 Self Invocation — 인지 및 해결 원칙

- **문제**: `this.internalMethod()` 호출 시 AOP 미적용 (프록시를 통한 호출만 적용)

**Rule**: **동일 클래스 내부 호출에는 AOP가 적용되지 않음을 명확히 인지.**

**해결 원칙(강제)**: Self-invocation 해결을 위해 **Proxy를 직접 다루는 코드**(`AopContext.currentProxy()`, `ObjectProvider`로 Proxy 주입 등)는 **금지**한다. 반드시 **물리적인 클래스(빈) 분리**를 통해 해결한다.

**대안**: 별도 빈으로 분리, Application Service → Domain Service 분리.

#### 3.5.12 문서화 필수

- **필수 문서 항목**: 적용 대상(Pointcut), 목적, 실행 시점, 예외 처리 정책, 제거 시 영향도

**Rule**: **AOP 추가 시 README 또는 ADR 문서 작성 필수.**

#### 3.5.13 테스트 규칙

- Aspect 단독 테스트는 최소화, **통합 테스트** 중심으로 실제 프록시 동작 검증
- 테스트 전용 Aspect는 `src/test/java` 하위에 분리 가능

#### 3.5.14 금지 패턴 (Anti-Patterns)

| 패턴              | 설명                        |
| ----------------- | --------------------------- |
| 비즈니스 로직 AOP | 흐름 파악 불가              |
| 범용 Pointcut     | 의도치 않은 적용            |
| God Aspect        | 책임 과다 (CommonAspect 등) |
| 상태 변경 Aspect  | 디버깅 악몽                 |

#### 3.5.15 코드 리뷰 체크리스트

- [ ] 이 로직은 AOP가 아닌 다른 방법으로 가능한가?
- [ ] 단일 책임을 지키는가?
- [ ] Pointcut 범위가 과도하지 않은가?
- [ ] @Around 사용이 정말 필요한가?
- [ ] 트랜잭션 경계가 명확한가?

---

#### AOP 원칙 요약

| #   | Rule                                                                |
| --- | ------------------------------------------------------------------- |
| 1   | AOP는 횡단 관심사 전용, 비즈니스 로직 금지                          |
| 2   | AOP 제거해도 시스템 정상 동작 (보조 역할)                           |
| 3   | Pointcut 패키지 기반 기본, Annotation 제한적, 메서드 이름 기반 금지 |
| 4   | Aspect 단일 책임, ~Aspect 접미사, aop 패키지                        |
| 5   | @Around 남용 금지, Advice 종류별 사용 기준                          |
| 6   | 예외 판단·삼키기 금지, 기록 후 재throw                              |
| 7   | 상태 변경·무거운 작업 금지, 관찰자 역할만                           |
| 8   | `@Transactional`은 Service 계층만                                   |
| 9   | 다중 AOP 시 `@Order` 필수, 메서드당 2~3개 이내                      |
| 10  | 성능 민감 영역은 적용 전 성능 검증                                  |
| 11  | Self Invocation 미적용 인지, Proxy 직접 다루기 금지·빈 분리로 해결  |
| 12  | AOP 추가 시 문서화(Pointcut·목적·영향도) 필수                       |
| 13  | 금지 패턴(Anti-Patterns) 회피                                       |

### 3.6 외부 라이브러리 관리 및 도입 규정 [SHOULD]

> 라이브러리 버전 혼재 방지, **안정성**·**보안** 확보, 팀원 간 일관된 사용 기준 제공.

#### 3.6.1 목적

- 라이브러리 버전의 혼재를 방지하여 **안정성**과 **보안**을 확보
- 팀원 간 일관된 사용 기준 제공
- 불필요한 라이브러리 도입을 최소화

#### 3.6.2 버전 선택 우선순위

1. **LTS (Long Term Support)** 또는 **Stable** 버전 (최우선)
2. **Latest Patch** 버전 (보안 패치가 포함된 최신)
3. **Latest Minor** (기능 추가가 필요할 때)
4. **Latest Major** (신규 프로젝트 또는 큰 리팩토링 시)

**절대 금지**: `latest`, `^99.0.0`, `next`, `beta`, `canary`, `dev` 채널 직접 사용

#### 3.6.3 도입 전 체크리스트

- 공식 GitHub의 **최근 6개월 내 커밋 활동** 확인
- **Weekly Downloads** (npm / Maven Central / pub.dev)
- **Maintainer 수** 및 **Sponsor** 여부
- **Known Vulnerabilities** (Dependabot, Snyk, OWASP)
- **License** (Apache-2.0, MIT, BSD 권장)
- **Breaking Change** 이력

#### 3.6.4 기술 스택별 버전 정책 (원칙)

- **구체적 버전 숫자**는 시간이 지남에 따라 낡으므로, RULE.md에는 **버전 선택 원칙**만 둔다.
- **실제 권장 버전**은 **TECH-STACK.md** 또는 **Version Catalog**(`libs.versions.toml`)에서 관리하고, 정기적으로 갱신한다.
- **원칙**: LTS/Stable 채널 우선, `latest`/floating tag 운영 환경 사용 금지, BOM/플랫폼으로 일괄 관리, 취약 버전 사용 금지.

#### 3.6.5 Spring Boot + Gradle 상세 규칙

- `build.gradle`에 **Version Catalog** (`libs.versions.toml`) **필수** 사용 권장
- Spring Boot는 **BOM**(`platform`)으로 관리
- `dependencyManagement` + `spring-boot-dependencies` 사용

#### 3.6.6 React / Next.js

- `package.json`에 **exact 버전**(`"react": "19.2.3"`) 또는 **tilde**(`~19.2.0`) 사용 권장
- `^` (caret)은 major upgrade 위험 때문에 신중히 사용
- `overrides` 또는 `resolutions` 사용 시 팀 리드 승인 필수

#### 3.6.7 Flutter

- `pubspec.yaml` → `dependencies`에 **caret(`^`)** 사용
- `flutter pub outdated` 주 1회 실행 권장
- `flutter channel stable` 고정

#### 3.6.8 신규 라이브러리 도입 프로세스 (필수)

1. **Issue** 생성 → `[Library Request]` 라벨
2. 아래 내용 포함:
   - 라이브러리 이름 + 버전 후보
   - 도입 목적 및 대안 검토
   - GitHub Stars, Weekly Download, License
   - 보안 취약점 여부
3. **팀 리드 또는 아키텍트 1인** 이상 승인 후 Merge

#### 3.6.9 문서화 의무

- `README.md` 또는 `ARCHITECTURE.md`에 **사용 중인 주요 라이브러리와 버전** 명시
- 코드 내에 라이브러리 사용 시 **링크 주석** 필수:

```java
// Kakao SDK - https://developers.kakao.com/sdk/js/kakao.js (v1.43.5)
// Spring Boot Actuator - https://docs.spring.io/spring-boot/docs/3.5.8/actuator.html
```

#### 3.6.10 업데이트 정책

| 구분           | 정책                                |
| -------------- | ----------------------------------- |
| **Patch 버전** | 언제든 업데이트 가능                |
| **Minor 버전** | 분기별 업데이트 검토                |
| **Major 버전** | 최소 2주 이상 PoC + 팀 승인 후 적용 |
