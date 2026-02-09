package com.example.sns.service.map;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.example.sns.config.MapProperties;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Google Maps Geocoding API 구현체.
 *
 * <p>PRD 2.2: 지도 API 추상화 구현체 (Google).
 * RULE 3.4: Timeout·Retry 정책 (MapProperties).
 * RULE 1.4.3: 외부 API 호출·실패 시 파라미터화 로깅.
 * Step 11: app.map.provider=google 시 활성화.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.map.provider", havingValue = "google")
public class GoogleMapServiceImpl implements MapService {

    private final MapProperties mapProperties;

    @Override
    public Optional<GeoResult> geocode(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        try {
            GeoApiContext context = createContext();
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            if (results != null && results.length > 0 && results[0].geometry != null) {
                LatLng location = results[0].geometry.location;
                log.debug("Geocoding 성공: address={}, lat={}, lng={}", address, location.lat, location.lng);
                return Optional.of(new GeoResult(location.lat, location.lng));
            }
        } catch (Exception e) {
            log.warn("Geocoding 실패: address={}, message={}", address, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> reverseGeocode(double latitude, double longitude) {
        try {
            GeoApiContext context = createContext();
            GeocodingResult[] results = GeocodingApi.reverseGeocode(context, new LatLng(latitude, longitude)).await();
            if (results != null && results.length > 0 && results[0].formattedAddress != null) {
                log.debug("Reverse Geocoding 성공: lat={}, lng={}", latitude, longitude);
                return Optional.of(results[0].formattedAddress);
            }
        } catch (Exception e) {
            log.warn("Reverse Geocoding 실패: lat={}, lng={}, message={}", latitude, longitude, e.getMessage());
        }
        return Optional.empty();
    }

    private GeoApiContext createContext() {
        String apiKey = mapProperties.googleApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Google Maps API Key가 설정되지 않았습니다. app.map.google-api-key 환경 변수를 확인하세요.");
        }
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(mapProperties.timeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(mapProperties.timeoutSeconds(), TimeUnit.SECONDS)
                .maxRetries(mapProperties.retryCount())
                .build();
    }
}
