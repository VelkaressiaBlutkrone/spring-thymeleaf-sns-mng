package com.example.sns.service.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.example.sns.config.JwtProperties;
import com.example.sns.domain.User;
import com.example.sns.domain.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 생성·검증 서비스.
 *
 * RULE 6.1: iss/aud/jti/exp 검증, alg allow-list(HS256), 민감정보 Payload 금지.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_USER_ID = "userId";

    private final JwtProperties jwtProperties;

    /**
     * Access Token 생성 (15분 이하).
     *
     * @param user 회원
     * @return accessToken, jti, expiresInSeconds
     */
    public TokenResult createAccessToken(User user) {
        String jti = UUID.randomUUID().toString();
        long expiresInSeconds = jwtProperties.getAccessTtlMinutes() * 60L;
        Date expiry = new Date(System.currentTimeMillis() + expiresInSeconds * 1000);

        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .id(jti)
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();

        return new TokenResult(token, jti, expiresInSeconds);
    }

    /**
     * Refresh Token용 JTI 생성 (Redis 저장용).
     */
    public String createRefreshTokenJti() {
        return UUID.randomUUID().toString();
    }

    /**
     * Access Token 검증 및 Claims 추출.
     *
     * @param token Bearer 토큰
     * @return Claims (sub=userId, role 등)
     * @throws JwtException 검증 실패 시
     */
    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(jwtProperties.getIssuer())
                .requireAudience(jwtProperties.getAudience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Access Token에서 남은 유효시간(초) 추출.
     */
    public long getRemainingSeconds(Claims claims) {
        Date exp = claims.getExpiration();
        if (exp == null) return 0;
        long remaining = (exp.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public record TokenResult(String token, String jti, long expiresInSeconds) {
    }
}
