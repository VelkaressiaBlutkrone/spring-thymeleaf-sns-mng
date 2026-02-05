package com.example.sns.service.auth;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis TokenStore Fallback 래퍼.
 *
 * Redis 연동 실패 시 NoOp 동작으로 전환 (Step 5.1).
 * RULE 5.2.1(Fallback), 5.3(기능 비활성화).
 */
@Slf4j
@Component
@Primary
@Profile("!test")
@RequiredArgsConstructor
public class FallbackTokenStore implements TokenStore {

    private final RedisTokenStore redisTokenStore;
    private final AtomicBoolean redisUnavailable = new AtomicBoolean(false);

    @Override
    public void saveRefreshToken(String jti, String payload, long ttlSeconds) {
        if (redisUnavailable.get()) {
            return;
        }
        try {
            redisTokenStore.saveRefreshToken(jti, payload, ttlSeconds);
        } catch (Exception e) {
            logRedisFailure("saveRefreshToken", e);
            redisUnavailable.set(true);
        }
    }

    @Override
    public Optional<String> getRefreshToken(String jti) {
        if (redisUnavailable.get()) {
            return Optional.empty();
        }
        try {
            return redisTokenStore.getRefreshToken(jti);
        } catch (Exception e) {
            logRedisFailure("getRefreshToken", e);
            redisUnavailable.set(true);
            return Optional.empty();
        }
    }

    @Override
    public void deleteRefreshToken(String jti) {
        if (redisUnavailable.get()) {
            return;
        }
        try {
            redisTokenStore.deleteRefreshToken(jti);
        } catch (Exception e) {
            logRedisFailure("deleteRefreshToken", e);
            redisUnavailable.set(true);
        }
    }

    @Override
    public void addToBlacklist(String jti, long ttlSeconds) {
        if (redisUnavailable.get()) {
            return;
        }
        try {
            redisTokenStore.addToBlacklist(jti, ttlSeconds);
        } catch (Exception e) {
            logRedisFailure("addToBlacklist", e);
            redisUnavailable.set(true);
        }
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (redisUnavailable.get()) {
            return false;
        }
        try {
            return redisTokenStore.isBlacklisted(jti);
        } catch (Exception e) {
            logRedisFailure("isBlacklisted", e);
            redisUnavailable.set(true);
            return false;
        }
    }

    private void logRedisFailure(String operation, Exception e) {
        log.warn("Redis TokenStore 실패, NoOp fallback 적용: operation={}, error={}", operation, e.getMessage());
    }
}
