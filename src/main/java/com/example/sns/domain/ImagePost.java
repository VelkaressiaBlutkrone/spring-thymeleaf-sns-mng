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
 * 이미지 게시글 엔티티.
 *
 * ERD: ImagePost (user_id FK, pin_id FK nullable, title, content, image_url, latitude, longitude, notice).
 * Step 9: 이미지+텍스트 게시글, image_url 저장 경로.
 */
@Entity
@Table(name = "image_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImagePost extends BaseEntity {

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

    /**
     * 저장된 이미지 파일 경로 (서버 내부 경로 또는 저장소 식별자).
     * 클라이언트에는 /api/image-posts/{id}/image 형태 URL로 노출.
     */
    @Column(nullable = false, length = 500, name = "image_url")
    private String imageStoragePath;

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
    public ImagePost(User author, String title, String content, String imageStoragePath,
                     Double latitude, Double longitude, Pin pin) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.imageStoragePath = imageStoragePath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pin = pin;
        onCreate();
    }

    /**
     * 제목·내용·이미지 경로 수정.
     */
    public void update(String title, String content, String imageStoragePath) {
        this.title = title;
        this.content = content;
        if (imageStoragePath != null && !imageStoragePath.isBlank()) {
            this.imageStoragePath = imageStoragePath;
        }
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
