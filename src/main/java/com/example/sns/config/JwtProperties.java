package com.example.sns.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 설정 속성.
 *
 * RULE 1.1: 비밀정보는 환경 변수로 주입.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private int accessTtlMinutes = 15;
    private int refreshTtlDays = 7;
    private String issuer = "https://api.example.com";
    private String audience = "spring-thymleaf-map-sns-mng";
    private String secretKey;
}
