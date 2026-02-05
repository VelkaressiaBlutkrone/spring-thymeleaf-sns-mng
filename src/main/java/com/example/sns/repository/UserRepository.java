package com.example.sns.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sns.domain.User;

/**
 * 회원 Repository.
 *
 * findByEmail: 중복 이메일 검증·로그인 시 사용.
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
}
