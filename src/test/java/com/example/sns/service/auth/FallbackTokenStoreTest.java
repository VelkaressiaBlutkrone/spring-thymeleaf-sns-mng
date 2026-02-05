package com.example.sns.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FallbackTokenStore 단위 테스트.
 *
 * Step 5.1: Redis 실패 시 NoOp fallback 검증.
 * RULE 4.2.2: Given-When-Then, AssertJ, BDDMockito 준수.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FallbackTokenStore 단위 테스트")
class FallbackTokenStoreTest {

    @Mock
    private RedisTokenStore redisTokenStore;

    @InjectMocks
    private FallbackTokenStore fallbackTokenStore;

    @Test
    @DisplayName("getRefreshToken - Redis 실패 시 empty를 반환한다")
    void getRefreshToken_Redis실패시_empty를_반환한다() {
        // given
        String jti = "jti-123";
        willThrow(new RuntimeException("Connection refused"))
                .given(redisTokenStore).getRefreshToken(jti);

        // when
        Optional<String> result = fallbackTokenStore.getRefreshToken(jti);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRefreshToken - Redis 실패 후 재호출 시에도 empty를 반환한다 (fallback 유지)")
    void getRefreshToken_Redis실패후_재호출시에도_empty를_반환한다() {
        // given
        String jti = "jti-123";
        willThrow(new RuntimeException("Connection refused"))
                .given(redisTokenStore).getRefreshToken(jti);

        // when
        fallbackTokenStore.getRefreshToken(jti);
        Optional<String> result = fallbackTokenStore.getRefreshToken(jti);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("saveRefreshToken - Redis 실패 시 예외 없이 완료한다")
    void saveRefreshToken_Redis실패시_예외없이_완료한다() {
        // given
        willThrow(new RuntimeException("Connection refused"))
                .given(redisTokenStore).saveRefreshToken(any(), any(), anyLong());

        // when & then
        fallbackTokenStore.saveRefreshToken("jti-1", "payload", 60L);
    }

    @Test
    @DisplayName("isBlacklisted - Redis 실패 시 false를 반환한다")
    void isBlacklisted_Redis실패시_false를_반환한다() {
        // given
        String jti = "jti-456";
        willThrow(new RuntimeException("Connection refused"))
                .given(redisTokenStore).isBlacklisted(jti);

        // when
        boolean result = fallbackTokenStore.isBlacklisted(jti);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getRefreshToken - Redis 정상 시 payload를 반환한다")
    void getRefreshToken_Redis정상시_payload를_반환한다() {
        // given
        String jti = "jti-123";
        String payload = "1:USER";
        given(redisTokenStore.getRefreshToken(jti)).willReturn(Optional.of(payload));

        // when
        Optional<String> result = fallbackTokenStore.getRefreshToken(jti);

        // then
        assertThat(result).isPresent().contains(payload);
    }
}
