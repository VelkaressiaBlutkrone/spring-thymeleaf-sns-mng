package com.example.sns.domain;

/**
 * 회원 역할.
 *
 * RULE 1.2: 인가 검증 시 사용.
 */
public enum UserRole {

    USER,
    ADMIN;

    /**
     * Spring Security 권한 문자열로 변환.
     */
    public String toAuthority() {
        return "ROLE_" + name();
    }
}
