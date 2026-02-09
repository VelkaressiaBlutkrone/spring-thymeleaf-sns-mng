/**
 * Service 계층 — 비즈니스 로직 및 트랜잭션 경계.
 *
 * Controller → Service → Repository 단방향 의존 (RULE 3.1).
 *
 * @Transactional은 이 계층에만 선언 (RULE 2.3, 3.5.7).
 */
package com.example.sns.service;
