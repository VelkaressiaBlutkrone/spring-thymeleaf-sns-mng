package com.example.sns.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 위도·경도 Value Object.
 *
 * ERD: Location — latitude, longitude 2개 컬럼. Embeddable로 표현.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
