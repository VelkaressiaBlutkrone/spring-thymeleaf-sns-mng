package com.example.sns.dto.response;

import java.time.LocalDateTime;

import com.example.sns.domain.User;

/**
 * 회원 응답 DTO.
 *
 * RULE 3.3: 엔티티 직접 반환 금지.
 */
public record MemberResponse(
        Long id,
        String email,
        String nickname,
        String role,
        LocalDateTime createdAt
) {

    public static MemberResponse from(User user) {
        return new MemberResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
