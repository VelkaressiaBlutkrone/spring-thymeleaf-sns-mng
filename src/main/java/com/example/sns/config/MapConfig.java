package com.example.sns.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 지도 API 설정.
 *
 * <p>Step 11: MapProperties 활성화. Timeout·Retry 정책 적용.
 */
@Configuration
@EnableConfigurationProperties(MapProperties.class)
public class MapConfig {
}
