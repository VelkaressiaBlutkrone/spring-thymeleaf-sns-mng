package com.example.sns.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
 * 지도 Pin 엔티티.
 *
 * ERD: Pin (user_id FK, description, latitude, longitude).
 * Step 10: Location VO 활용, Pin↔Post/ImagePost 연관(Post·ImagePost가 Pin 참조).
 */
@Entity
@Table(name = "pins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(length = 500)
    private String description;

    @Embedded
    private Location location;

    @Builder
    public Pin(User owner, String description, Double latitude, Double longitude) {
        this.owner = owner;
        this.description = description;
        this.location = new Location(latitude, longitude);
        onCreate();
    }

    public void update(String description, Double latitude, Double longitude) {
        this.description = description;
        if (latitude != null && longitude != null) {
            this.location = new Location(latitude, longitude);
        }
        onUpdate();
    }

    public boolean isOwner(User user) {
        return user != null && owner != null && owner.getId().equals(user.getId());
    }

    public Double getLatitude() {
        return location != null ? location.getLatitude() : null;
    }

    public Double getLongitude() {
        return location != null ? location.getLongitude() : null;
    }
}
