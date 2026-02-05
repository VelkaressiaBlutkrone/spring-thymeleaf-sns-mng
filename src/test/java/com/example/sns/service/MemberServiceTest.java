package com.example.sns.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.sns.domain.User;
import com.example.sns.domain.UserRole;
import com.example.sns.dto.request.MemberJoinRequest;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.repository.UserRepository;

/**
 * MemberService 단위 테스트.
 *
 * RULE 4.2.2: Given-When-Then, AssertJ, BDDMockito 준수.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("join - 정상 요청 시 회원을 생성하고 MemberResponse를 반환한다")
    void join_정상요청시_회원을생성하고_MemberResponse를_반환한다() {
        // given
        MemberJoinRequest request = new MemberJoinRequest(
                "test@example.com",
                "password123",
                "테스트닉네임"
        );
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("hashedPassword");
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User user = inv.getArgument(0);
            User saved = User.builder()
                    .email(user.getEmail())
                    .passwordHash(user.getPasswordHash())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .build();
            try {
                var idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(saved, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return saved;
        });

        // when
        var result = memberService.join(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.nickname()).isEqualTo("테스트닉네임");
        assertThat(result.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("join - 중복 이메일 시 DUPLICATE_EMAIL 예외를 던진다")
    void join_중복이메일시_DUPLICATE_EMAIL_예외를_던진다() {
        // given
        MemberJoinRequest request = new MemberJoinRequest(
                "duplicate@example.com",
                "password123",
                "닉네임"
        );
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_EMAIL));
    }
}
