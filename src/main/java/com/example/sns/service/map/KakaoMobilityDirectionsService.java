package com.example.sns.service.map;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.sns.config.MapProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kakao Mobility Directions API — 실제 도로 경로·이동 거리 조회.
 * https://developers.kakaomobility.com/docs/navi-api/directions/
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMobilityDirectionsService {

    private static final String API_URL = "https://apis-navi.kakaomobility.com/v1/directions";

    private final MapProperties mapProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 출발지→목적지 실제 도로 경로와 거리 조회.
     *
     * @param originLat  출발지 위도
     * @param originLng  출발지 경도
     * @param destLat    목적지 위도
     * @param destLng    목적지 경도
     * @return 경로 좌표 리스트 + 이동 거리(미터), 실패 시 null
     */
    public DirectionsResult getDirections(double originLat, double originLng, double destLat, double destLng) {
        String apiKey = mapProperties.kakaoMobilityApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Kakao Mobility API 키 미설정. 직선 거리만 사용 가능.");
            return null;
        }

        // origin, destination: Kakao Mobility uses "x,y" = lng,lat
        String origin = originLng + "," + originLat;
        String destination = destLng + "," + destLat;

        String url = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .queryParam("summary", false)
                .build()
                .toUriString();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);
            headers.set("Content-Type", "application/json");
            // KA Header: 서버 호출 시 os 또는 origin 필수 (401 방지)
            // os/javascript + origin: 웹앱에서의 호출로 간주
            headers.set("KA", "sdk/1.0 os/javascript origin/http://localhost:5173");
            headers.set("Origin", "http://localhost:5173");

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    JsonNode.class);

            JsonNode body = response.getBody();
            if (body == null || !body.has("routes")) {
                return null;
            }

            JsonNode routes = body.get("routes");
            if (routes.isEmpty()) {
                return null;
            }

            JsonNode route = routes.get(0);
            int resultCode = route.path("result_code").asInt();
            if (resultCode != 0) {
                log.debug("Kakao Mobility 경로 탐색 실패: result_code={}", resultCode);
                return null;
            }

            int distanceMeters = route.path("summary").path("distance").asInt();
            List<double[]> path = new ArrayList<>();

            JsonNode sections = route.path("sections");
            for (JsonNode section : sections) {
                JsonNode roads = section.path("roads");
                for (JsonNode road : roads) {
                    JsonNode vertexes = road.path("vertexes");
                    if (vertexes.isArray()) {
                        for (int i = 0; i < vertexes.size(); i += 2) {
                            if (i + 1 < vertexes.size()) {
                                double lng = vertexes.get(i).asDouble();
                                double lat = vertexes.get(i + 1).asDouble();
                                path.add(new double[] { lat, lng });
                            }
                        }
                    }
                }
            }

            return new DirectionsResult(path, distanceMeters);
        } catch (Exception e) {
            log.warn("Kakao Mobility 경로 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    public record DirectionsResult(List<double[]> path, int distanceMeters) {
    }
}
