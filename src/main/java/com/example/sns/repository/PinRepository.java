package com.example.sns.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sns.domain.Pin;
import com.example.sns.domain.User;

/**
 * Pin Repository.
 *
 * Step 10: 사용자별 목록·상세.
 * Step 11: 반경 내 Pin 조회 (Haversine 공식).
 */
public interface PinRepository extends JpaRepository<Pin, Long> {

    Page<Pin> findByOwner(User owner, Pageable pageable);

    /**
     * 반경(km) 내 Pin 조회. Haversine 공식 사용.
     * H2·MySQL 호환.
     *
     * @param radiusKm 반경(km)
     * @param lat      중심 위도
     * @param lng      중심 경도
     * @param pageable 페이징
     * @return 반경 내 Pin 목록
     */
    @Query(value = """
            SELECT * FROM pins p
            WHERE 6371 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(:lat - p.latitude) / 2), 2) +
                COS(RADIANS(p.latitude)) * COS(RADIANS(:lat)) *
                POWER(SIN(RADIANS(:lng - p.longitude) / 2), 2)
            )) <= :radiusKm
            """,
            countQuery = """
            SELECT COUNT(*) FROM pins p
            WHERE 6371 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(:lat - p.latitude) / 2), 2) +
                COS(RADIANS(p.latitude)) * COS(RADIANS(:lat)) *
                POWER(SIN(RADIANS(:lng - p.longitude) / 2), 2)
            )) <= :radiusKm
            """,
            nativeQuery = true)
    Page<Pin> findWithinRadius(@Param("radiusKm") double radiusKm, @Param("lat") double lat, @Param("lng") double lng,
                               Pageable pageable);
}
