package com.example.sns.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sns.domain.Post;
import com.example.sns.domain.User;

/**
 * 게시글 Repository.
 *
 * Step 8: 목록(페이징·검색)·상세.
 * Step 11: 반경 내 게시글 조회 (위치 있는 글만).
 * Step 12: Pin별 게시글 목록 (지도 Pin 클릭 시).
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 작성자별 게시글 목록. Step 14: 마이페이지 내 게시글.
     */
    Page<Post> findByAuthor(User author, Pageable pageable);

    /**
     * Pin에 연결된 게시글 목록. Step 12: 지도 Pin 클릭 시 관련 글 표시.
     */
    Page<Post> findByPin_Id(Long pinId, Pageable pageable);

    /**
     * 제목 또는 내용에 키워드 포함 검색. 키워드 없으면 전체 조회.
     */
    Page<Post> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);

    default Page<Post> findAllByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return findAll(pageable);
        }
        String trimmed = keyword.trim();
        return findByTitleContainingOrContentContaining(trimmed, trimmed, pageable);
    }

    /**
     * 반경(km) 내 게시글 조회. latitude·longitude가 있는 글만.
     * Haversine 공식 사용. H2·MySQL 호환.
     */
    @Query(value = """
            SELECT * FROM posts p
            WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL
            AND 6371 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(:lat - p.latitude) / 2), 2) +
                COS(RADIANS(p.latitude)) * COS(RADIANS(:lat)) *
                POWER(SIN(RADIANS(:lng - p.longitude) / 2), 2)
            )) <= :radiusKm
            """,
            countQuery = """
            SELECT COUNT(*) FROM posts p
            WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL
            AND 6371 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(:lat - p.latitude) / 2), 2) +
                COS(RADIANS(p.latitude)) * COS(RADIANS(:lat)) *
                POWER(SIN(RADIANS(:lng - p.longitude) / 2), 2)
            )) <= :radiusKm
            """,
            nativeQuery = true)
    Page<Post> findWithinRadius(@Param("radiusKm") double radiusKm, @Param("lat") double lat, @Param("lng") double lng,
                                Pageable pageable);

    /**
     * 기간 내 게시글 수를 일별로 집계. Step 17. (MySQL/H2 DATE() 호환)
     */
    @Query(value = "SELECT DATE(created_at), COUNT(*) FROM posts WHERE created_at BETWEEN :start AND :end GROUP BY DATE(created_at)",
            nativeQuery = true)
    List<Object[]> countByCreatedAtBetweenGroupByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
