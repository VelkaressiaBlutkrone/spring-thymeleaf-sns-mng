package com.example.sns.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Redis 설정.
 *
 * 연결 검증·로깅은 ConnectionHealthConfig에서 수행.
 * RULE 1.1: 비밀정보(비밀번호 등)는 환경 변수로 주입.
 */
@Configuration
@Profile("!test")
public class RedisConfig {
}
