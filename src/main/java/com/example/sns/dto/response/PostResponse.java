package com.example.sns.dto.response;

import java.time.LocalDateTime;

import com.example.sns.domain.Post;

/**
 * 게시글 응답 DTO.
 *
 * RULE 3.3: 엔티티 직접 반환 금지.
 * API 명세 PostResponse.
 */
public record PostResponse(
        Long id,
        Long authorId,
        String authorNickname,
        String title,
        String content,
        Double latitude,
        Double longitude,
        Long pinId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getTitle(),
                post.getContent(),
                post.getLatitude(),
                post.getLongitude(),
                post.getPin() != null ? post.getPin().getId() : null,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
