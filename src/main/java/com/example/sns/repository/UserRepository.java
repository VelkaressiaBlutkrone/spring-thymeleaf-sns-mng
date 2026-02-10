package com.example.sns.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sns.domain.User;

/**
 * 회원 Repository.
 *
 * findByEmail: 중복 이메일 검증·로그인 시 사용.
 * Step 15: 관리자 회원 목록 검색(이메일·닉네임).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 회원 조회.
     *
     * @param email 이메일 (unique)
     * @return 회원 Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부.
     *
     * @param email 이메일
     * @return 존재 시 true
     */
    boolean existsByEmail(String email);

    /**
     * 이메일 또는 닉네임으로 검색. Step 15: 관리자 회원 목록.
     */
    Page<User> findByEmailContainingOrNicknameContaining(String emailKeyword, String nicknameKeyword,
                                                         Pageable pageable);

    default Page<User> findAllByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return findAll(pageable);
        }
        String trimmed = keyword.trim();
        return findByEmailContainingOrNicknameContaining(trimmed, trimmed, pageable);
    }

    /**
     * 기간 내 가입자 수. Step 17: 관리자 가입 통계.
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
