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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 엔티티.
 *
 * ERD: Post (user_id FK, pin_id FK nullable, title, content, latitude, longitude, notice).
 * Step 8: 작성 위치(위도·경도) 저장.
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id")
    private Pin pin;

    @Column(nullable = false)
    private boolean notice = false;

    @Builder
    public Post(User author, String title, String content, Double latitude, Double longitude, Pin pin) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pin = pin;
        onCreate();
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        onUpdate();
    }

    public boolean isAuthor(User user) {
        return user != null && author != null && author.getId().equals(user.getId());
    }

    /**
     * 공지 여부 설정. Step 16: 관리자 공지 등록/해제.
     */
    public void setNotice(boolean notice) {
        this.notice = notice;
        onUpdate();
    }
}
