package com.example.sns.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sns.domain.Pin;
import com.example.sns.domain.User;

/**
 * Pin Repository.
 *
 * Step 10: 사용자별 목록·상세.
 */
public interface PinRepository extends JpaRepository<Pin, Long> {

    Page<Pin> findByOwner(User owner, Pageable pageable);
}
