package com.example.sns.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 이력. Step 17: 관리자 로그인 통계용.
 */
@Entity
@Table(name = "login_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private java.time.LocalDateTime loggedAt;

    /** 로그인 성공 시 호출. Step 17. */
    public static LoginLog of(User user) {
        return new LoginLog(user, java.time.LocalDateTime.now());
    }

    public LoginLog(User user, java.time.LocalDateTime loggedAt) {
        this.user = user;
        this.loggedAt = loggedAt;
    }
}
