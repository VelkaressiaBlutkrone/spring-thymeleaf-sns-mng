package com.example.sns.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sns.domain.ImagePost;
import com.example.sns.domain.User;

/**
 * 이미지 게시글 Repository.
 *
 * Step 9: 목록(페이징·검색)·상세·작성자별 조회.
 * Step 11: 반경 내 이미지 게시글 조회 (위치 있는 글만).
 */
public interface ImagePostRepository extends JpaRepository<ImagePost, Long> {

    /**
     * 제목 또는 내용에 키워드 포함 검색. 키워드 없으면 전체 조회.
     */
    Page<ImagePost> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);

    Page<ImagePost> findByAuthor(User author, Pageable pageable);

    default Page<ImagePost> findAllByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return findAll(pageable);
        }
        String trimmed = keyword.trim();
        return findByTitleContainingOrContentContaining(trimmed, trimmed, pageable);
    }

    /**
     * 반경(km) 내 이미지 게시글 조회. latitude·longitude가 있는 글만.
     * Haversine 공식 사용. H2·MySQL 호환.
     */
    @Query(value = """
            SELECT * FROM image_posts ip
            WHERE ip.latitude IS NOT NULL AND ip.longitude IS NOT NULL
            AND 6371 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(:lat - ip.latitude) / 2), 2) +
                COS(RADIANS(ip.latitude)) * COS(RADIANS(:lat)) *
                POWER(SIN(RADIANS(:lng - ip.longitude) / 2), 2)
            )) <= :radiusKm
            """,
            countQuery = """
            SELECT COUNT(*) FROM image_posts ip
            WHERE ip.latitude IS NOT NULL AND ip.longitude IS NOT NULL
            AND 6371 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(:lat - ip.latitude) / 2), 2) +
                COS(RADIANS(ip.latitude)) * COS(RADIANS(:lat)) *
                POWER(SIN(RADIANS(:lng - ip.longitude) / 2), 2)
            )) <= :radiusKm
            """,
            nativeQuery = true)
    Page<ImagePost> findWithinRadius(@Param("radiusKm") double radiusKm, @Param("lat") double lat,
                                     @Param("lng") double lng, Pageable pageable);
}
