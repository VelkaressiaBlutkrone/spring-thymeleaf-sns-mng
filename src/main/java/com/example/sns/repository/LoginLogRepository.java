package com.example.sns.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sns.domain.LoginLog;

/**
 * 로그인 이력 Repository. Step 17: 관리자 로그인 통계.
 */
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    long countByLoggedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT l.user.id) FROM LoginLog l WHERE l.loggedAt BETWEEN :start AND :end")
    long countDistinctUserIdByLoggedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
