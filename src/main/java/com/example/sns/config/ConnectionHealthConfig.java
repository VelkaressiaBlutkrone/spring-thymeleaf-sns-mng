package com.example.sns.config;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * DB·Redis 연결 검증 및 로깅.
 *
 * Step 5.1: 연결 성공/실패 로깅, Redis 실패 시에도 서버 기동 유지.
 * RULE 1.4.3: 파라미터화 로깅, 비밀정보 제외.
 * RULE 5.2.1: Fallback 전략.
 */
@Slf4j
@Configuration
@Profile("!test")
public class ConnectionHealthConfig {

    /**
     * DB 연결 검증: DataSource ping 수행.
     * 성공 시 INFO, 실패 시 ERROR 로깅. 실패해도 throw 하지 않음 (서버 기동 유지).
     */
    @Bean
    public ApplicationRunner dbConnectionHealthLogger(DataSource dataSource) {
        return args -> {
            try {
                try (var conn = dataSource.getConnection()) {
                    boolean valid = conn.isValid(3);
                    if (valid) {
                        log.info("DB 연결 성공");
                    } else {
                        log.error("DB 연결 검증 실패: isValid=false");
                    }
                }
            } catch (Exception e) {
                log.error("DB 연결 실패: {}. DB 기반 API 호출 시 503 반환.", e.getMessage());
            }
        };
    }

    /**
     * Redis 연결 검증: ping 수행.
     * 성공 시 INFO, 실패 시 ERROR 로깅. 실패해도 throw 하지 않음 (서버 기동 유지).
     */
    @Bean
    public ApplicationRunner redisConnectionHealthLogger(StringRedisTemplate redisTemplate) {
        return args -> {
            try {
                String pong = redisTemplate.getConnectionFactory().getConnection().ping();
                log.info("Redis 연결 성공: pong={}", pong);
            } catch (Exception e) {
                log.error("Redis 연결 실패: {}. TokenStore NoOp fallback 적용.", e.getMessage());
            }
        };
    }
}
