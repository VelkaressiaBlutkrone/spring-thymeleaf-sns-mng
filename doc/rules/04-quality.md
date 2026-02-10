# 4. 품질(Quality) RULE — 유지보수 가능성 [MUST]

> 원본: [RULE.md](../RULE.md) 4장.

### 4.1 코드 규칙

#### 4.1.1 코드 작성

- 메서드 하나당 **책임 하나**
- 메서드 길이 과도 금지 (보통 **30~40줄 기준**)
- **매직 넘버, 문자열 상수화**

### 4.2 테스트 최소 기준

#### 4.2.1 테스트 원칙

- **핵심 로직**에 대한 테스트 존재. (핵심 로직: 비즈니스 규칙·도메인 정책·결정을 내리는 서비스/유틸 코드. 팀에서 "핵심" 범위를 정의해 두는 것을 권장.)
- **테스트 없는 핵심 로직 배포 금지**
- **커버리지 목표**(선택): 라인 커버리지 70% 이상, 브랜치 커버리지 60% 이상 등 팀 합의 목표를 두면 좋다. RULE 레벨로 강제하지 않을 수 있으며, 프로젝트 규모에 따라 조정.
- 테스트는 외부 환경(DB, API)에 의존하지 않는다.
- **슬라이스/통합 테스트 시 DB**: In-memory(H2) 또는 **Testcontainers**를 통한 격리된 환경만 사용한다. **실제 운영/개발 DB 연결은 금지**한다.

#### 4.2.1.1 테스트 결정성 보장

- 테스트 코드에서 `System.currentTimeMillis()`, `LocalDateTime.now()` **직접 사용 금지**
- `Clock`, `TimeProvider` 등으로 **추상화**
- 랜덤값은 **고정 시드** 또는 **Stub** 사용

> CI에서만 깨지는 테스트 원인 1순위

#### 4.2.2 테스트 코드 작성 규칙 (2025~2026 베스트 프랙티스)

> 모든 단위 테스트·슬라이스 테스트는 아래 규칙을 준수한다. 예외가 필요한 경우 기술 리더 승인 및 문서화를 거친다.

##### 4.2.2.1 기본 원칙

- **Given-When-Then 패턴 필수**: 모든 단위 테스트·슬라이스 테스트는 3단계 구조를 반드시 준수한다.
- **주석 구분**: 테스트 메서드 본문에 `// given`, `// when`, `// then` 주석으로 명시적으로 3단계를 구분한다.
- **AssertJ 사용**: 결과 검증에는 JUnit 기본 Assertions 대신 **AssertJ**를 사용한다.
- **Mock 라이브러리**: Java → **Mockito**, Kotlin → **MockK** 사용.
- **테스트 메서드 이름**: 행위 중심 + 결과 중심, **BDD 스타일** 권장.

##### 4.2.2.2 테스트 유형별 적용 범위

| 테스트 유형                         | 사용 어노테이션 조합                          | 목 객체 사용      | Given-When-Then 필수    | 추천 Assert       | 비고                          |
| ----------------------------------- | --------------------------------------------- | ----------------- | ----------------------- | ----------------- | ----------------------------- |
| 순수 단위 테스트 (Service, Util 등) | `@ExtendWith(MockitoExtension.class)`         | Mockito / MockK   | 필수                    | AssertJ           | Spring 컨텍스트 로드 X        |
| Repository 슬라이스 테스트          | `@DataJpaTest` + `@AutoConfigureTestDatabase` | 필요 시 Mock      | 필수                    | AssertJ           | H2 또는 Testcontainers만 사용 |
| Controller 슬라이스 테스트          | `@WebMvcTest` + `@MockBean`                   | 필수 (Service 등) | 필수                    | AssertJ + MockMvc |                               |
| 전체 통합 테스트                    | `@SpringBootTest` + `@AutoConfigureMockMvc`   | 최소화            | 권장 (복잡할 경우 필수) | AssertJ           | 느리므로 최소화               |

##### 4.2.2.3 테스트 메서드 이름 규칙 (강력 추천)

- **BDD 스타일**: `[메서드명]_[상황설명]_should[기대결과]` 또는 `should[기대결과]_when[상황]`
- **한글 메서드명** 예시: `findById_존재하는ID_주면_해당회원을반환한다`, `register_중복이메일이면_예외를던진다`
- **대안(CI·빌드 도구 호환)**: 일부 환경에서 한글 메서드명이 깨지거나 문제를 일으킬 수 있으므로, **메서드명은 영문**으로 두고 **`@DisplayName`에 한글**을 쓰는 방식을 동등하게 허용한다. (예: `@DisplayName("존재하는 ID를 주면 해당 회원을 반환한다")` + 메서드명 `findById_withValidId_returnsMember`)
- Kotlin: `snake_case` 또는 자연어 스타일 허용 (팀 결정)

##### 4.2.2.4 코드 구조 템플릿 (Java + JUnit 5 + Mockito + AssertJ)

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원 서비스 단위 테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("findById - 존재하는 ID로 조회하면 해당 회원을 반환한다")
    void findById_존재하는ID_주면_해당회원을반환한다() {
        // given
        Long memberId = 1L;
        Member expected = Member.builder()
                .id(memberId)
                .email("test@example.com")
                .nickname("테스트유저")
                .build();

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(expected));

        // when
        Member actual = memberService.findById(memberId);

        // then
        assertThat(actual)
                .isNotNull()
                .extracting("id", "email", "nickname")
                .containsExactly(memberId, "test@example.com", "테스트유저");
    }

    @Test
    @DisplayName("register - 이미 존재하는 이메일이면 예외를 던진다")
    void register_중복이메일이면_예외를던진다() {
        // given
        MemberCreateRequest request = MemberCreateRequest.builder()
                .email("duplicate@example.com")
                .build();

        given(memberRepository.existsByEmail(request.getEmail()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용중인 이메일입니다.");
    }
}
```

##### 4.2.2.5 Kotlin + MockK 템플릿 (선택)

```kotlin
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk

@ExtendWith(MockitoExtension::class)  // 또는 Kotest + MockK 조합
@DisplayName("회원 서비스")
class MemberServiceTest {

    private val memberRepository: MemberRepository = mockk()
    private val memberService = MemberService(memberRepository)

    @Test
    fun `이메일로 회원 조회 - 존재하면 회원 반환`() {
        // given
        val email = "test@example.com"
        val expected = Member(id = 1L, email = email, nickname = "테스트")

        every { memberRepository.findByEmail(email) } returns expected

        // when
        val actual = memberService.findByEmail(email)

        // then
        assertThat(actual).isEqualTo(expected)
    }
}
```

##### 4.2.2.6 추가 강제 규칙

- **주석 강제**: 모든 테스트에 `// given`, `// when`, `// then` 3줄 주석 필수 (가독성 극대화)
- **BDDMockito 권장**: `given(...).willReturn(...)` 형식 사용 (`when(...).thenReturn(...)` 대신)
- **AssertJ 체이닝**: `extracting()`, `hasFieldOrPropertyWithValue()`, `satisfies()` 등 적극 활용
- **@DisplayName 필수**: 테스트 클래스와 메서드 모두에 의미 있는 한글 설명 작성
- **예외 테스트**: `assertThatThrownBy()` 사용
- **단일 책임**: 한 테스트 메서드 = 한 시나리오

### 4.3 문서 규칙

#### 4.3.1 문서화 기준

- 공개 API → **Swagger/OpenAPI 필수**
- 설정 값 → README 또는 config 문서화
- ❌ "코드 보면 안다" 금지

#### 4.3.2 Task 문서 작성 구조 (`doc/TASK.md`)

> Task 문서(`doc/TASK.md`)를 **새로 작성**하거나 **Step을 추가**할 때 반드시 아래 구조를 준수한다.

##### 4.3.2.1 필수 필드 정의

| 필드               | 설명                                     |
| ------------------ | ---------------------------------------- |
| **Step Name**      | 단계 이름                                |
| **Step Goal**      | 이 단계를 끝냈을 때 달성할 목표(한 문장) |
| **Input**          | 이 단계에 필요한 입력(문서·코드·환경 등) |
| **Scope**          | 포함/제외로 단계 범위 명시               |
| **Instructions**   | 수행할 작업 목록                         |
| **Output Format**  | 산출물 형태·위치·형식                    |
| **Constraints**    | 지켜야 할 제약(RULE·기술 등)             |
| **Done When**      | 아래 조건이 충족되면 단계 완료로 간주    |
| **Duration**       | 예상 소요일수                            |
| **RULE Reference** | 참조할 RULE.md 섹션                      |

##### 4.3.2.2 강제 사항

- 신규 Step 추가 시 위 **10개 필드를 모두 작성**한다.
- 기존 Step 수정 시 해당 필드가 있다면 내용을 **갱신**한다.
- 필드 누락 시 코드 리뷰에서 보완 요청 대상이 된다.

### 4.4 주석 규칙 (v1.0 — 2026.02) [SHOULD]

> 주석은 Why를 설명한다. What은 코드로 표현한다.
> **레벨**: public API·외부 노출 메서드에 대한 Javadoc은 MUST에 가깝게 적용하고, 그 외는 SHOULD. 팀·프로젝트 규모에 따라 예외를 두어도 된다.

#### 4.4.1 기본 원칙

- 주석은 **Why(이유)** 를 설명한다. What(무엇)은 코드로 표현한다.
- **한글 주석**을 기본으로 한다 (영어 허용하나 프로젝트 내 일관성 유지)
- 주석은 코드와 **동기화**되어야 한다.

#### 4.4.2 문서화 주석 (Javadoc)

- **public API**(클래스, 메서드, 인터페이스) → 필수
- Javadoc 형식 준수
- 구조: 한 줄 요약 → 빈 줄 → 상세 설명 → 태그 순서 (`@param` → `@return` → `@throws` → `@example`)

```java
/**
 * 회원 이메일로 조회한다.
 *
 * @param email 이메일 (unique)
 * @return 회원 Optional
 */
Optional<User> findByEmail(String email);
```

#### 4.4.3 구현 주석 (Inline / Block)

- **비직관적인 로직**에만 작성
- `//` 한 줄 주석 선호
- 블록 주석 `/* */` 사용 시 `*` 정렬 유지

#### 4.4.4 금지 사항

- ❌ 코드와 동일한 내용 반복 (예: `i++` // i를 1 증가시킴)
- ❌ 주석 박스(`*********`) 사용 금지
- ❌ 불필요한 주석으로 코드 가독성 저하 금지

#### 4.4.5 언어별 세부 규칙

- **Java/Kotlin** → Google Java Style Guide + Javadoc
- **JavaScript/TypeScript** → JSDoc + Airbnb 스타일
- **Python** → PEP 8 + Google Python Style Guide docstring

#### 4.4.6 린팅·검증

- Checkstyle, SpotBugs 등에 주석 규칙 플러그인 적용 예정
