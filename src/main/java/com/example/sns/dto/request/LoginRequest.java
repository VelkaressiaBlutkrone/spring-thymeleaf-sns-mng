package com.example.sns.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO.
 *
 * RULE 1.3: 입력값 검증 (@Valid).
 */
public record LoginRequest(

        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
