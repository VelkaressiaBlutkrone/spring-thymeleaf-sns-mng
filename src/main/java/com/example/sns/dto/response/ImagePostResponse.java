package com.example.sns.dto.response;

import java.time.LocalDateTime;

import com.example.sns.domain.ImagePost;

/**
 * 이미지 게시글 응답 DTO.
 *
 * RULE 3.3: 엔티티 직접 반환 금지.
 * API 명세 ImagePostResponse. imageUrl은 클라이언트용 URL (/api/image-posts/{id}/image).
 */
public record ImagePostResponse(
        Long id,
        Long authorId,
        String authorNickname,
        String title,
        String content,
        String imageUrl,
        Double latitude,
        Double longitude,
        Long pinId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * 엔티티에서 응답 DTO 생성. imageUrl은 /api/image-posts/{id}/image 형태.
     */
    public static ImagePostResponse from(ImagePost post) {
        String imageUrl = "/api/image-posts/" + post.getId() + "/image";
        return new ImagePostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getTitle(),
                post.getContent(),
                imageUrl,
                post.getLatitude(),
                post.getLongitude(),
                post.getPin() != null ? post.getPin().getId() : null,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
