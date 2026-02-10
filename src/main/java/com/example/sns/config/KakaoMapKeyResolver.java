package com.example.sns.config;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 카카오맵 JavaScript API 키 조회.
 * application-local.yml(app.map.kakao-js-app-key) 또는 환경 변수 MAP_KAKAO_JS_APP_KEY.
 */
@Component
@RequiredArgsConstructor
public class KakaoMapKeyResolver {

    private static final String ENV_MAP_KAKAO_JS_APP_KEY = "MAP_KAKAO_JS_APP_KEY";

    private final MapProperties mapProperties;

    /**
     * dev 로컬 기동 시 application-local.yml 또는 MAP_KAKAO_JS_APP_KEY 에서 키 반환.
     */
    public String resolve() {
        String fromProps = mapProperties.kakaoJsAppKey();
        if (fromProps != null && !fromProps.isBlank()) {
            return fromProps.trim();
        }
        String fromEnv = System.getenv(ENV_MAP_KAKAO_JS_APP_KEY);
        return fromEnv != null && !fromEnv.isBlank() ? fromEnv.trim() : "";
    }
}
