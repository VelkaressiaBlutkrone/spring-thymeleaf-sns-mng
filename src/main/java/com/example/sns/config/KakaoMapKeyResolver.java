package com.example.sns.config;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카카오맵 JavaScript API 키 조회.
 * <p>우선순위: application-local.yml (profile local) → 환경 변수 MAP_KAKAO_JS_APP_KEY → JVM 시스템 프로퍼티 MAP_KAKAO_JS_APP_KEY.
 * 로컬 실행 시 dev 프로파일 + optional:file:./application-local.yml 오버라이드로 카카오맵 키 적용.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMapKeyResolver {

    private static final String ENV_MAP_KAKAO_JS_APP_KEY = "MAP_KAKAO_JS_APP_KEY";

    private final MapProperties mapProperties;

    @PostConstruct
    void logKeyStatus() {
        String key = resolve();
        if (key.isEmpty()) {
            log.warn("카카오맵 API 키 미설정. 지도 페이지에서 placeholder만 표시됩니다. doc/trouble/08-application-profile-kakao-map.md 참고.");
        } else {
            log.info("카카오맵 API 키 적용됨 (키 길이: {}).", key.length());
        }
    }

    /**
     * dev 로컬 기동 시 application-local.yml(profile local) 또는 MAP_KAKAO_JS_APP_KEY(env/system) 에서 키 반환.
     */
    public String resolve() {
        String fromProps = mapProperties.kakaoJsAppKey();
        if (fromProps != null && !fromProps.isBlank()) {
            return fromProps.trim();
        }
        String fromEnv = System.getenv(ENV_MAP_KAKAO_JS_APP_KEY);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromSys = System.getProperty(ENV_MAP_KAKAO_JS_APP_KEY);
        if (fromSys != null && !fromSys.isBlank()) {
            return fromSys.trim();
        }
        return "";
    }
}
