package com.example.sns.service.map;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 지도 API 미사용 구현체 (NoOp).
 *
 * <p>외부 지도 API 키가 없거나 비활성화 시 사용.
 * Geocoding·Reverse Geocoding 호출 시 항상 empty 반환.
 * RULE 3.5: AOP 제거/비활성화 시에도 시스템 정상 동작.
 * Step 11: app.map.provider=none 또는 미설정 시 활성화.
 */
@Service
@ConditionalOnProperty(name = "app.map.provider", havingValue = "none", matchIfMissing = true)
public class NoOpMapService implements MapService {

    @Override
    public Optional<GeoResult> geocode(String address) {
        return Optional.empty();
    }

    @Override
    public Optional<String> reverseGeocode(double latitude, double longitude) {
        return Optional.empty();
    }
}
