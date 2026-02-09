package com.example.sns.service.map;

import java.util.Optional;

/**
 * 지도 API 추상화 인터페이스.
 *
 * <p>PRD 2.2: Kakao/Naver/Google Map API 추상화. 구현체는 프로퍼티로 선택.
 * RULE 3.4: 외부 호출 시 Timeout·Retry 정책 적용.
 * Step 11: Geocoding·Reverse Geocoding 등 지도 관련 외부 API 호출 추상화.
 *
 * @see GoogleMapServiceImpl
 * @see NoOpMapService
 */
public interface MapService {

    /**
     * 주소를 위·경도로 변환 (Geocoding).
     *
     * @param address 주소 문자열
     * @return 위도·경도 (주소를 찾지 못하면 empty)
     */
    Optional<GeoResult> geocode(String address);

    /**
     * 위·경도를 주소로 변환 (Reverse Geocoding).
     *
     * @param latitude  위도
     * @param longitude 경도
     * @return 주소 문자열 (찾지 못하면 empty)
     */
    Optional<String> reverseGeocode(double latitude, double longitude);

    /**
     * Geocoding 결과 (위도·경도).
     *
     * @param latitude  위도
     * @param longitude 경도
     */
    record GeoResult(double latitude, double longitude) {
    }
}
