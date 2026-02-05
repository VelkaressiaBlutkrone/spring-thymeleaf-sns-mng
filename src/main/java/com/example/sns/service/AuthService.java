package com.example.sns.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.sns.domain.User;
import com.example.sns.dto.request.LoginRequest;
import com.example.sns.dto.response.LoginResponse;
import com.example.sns.dto.response.MemberResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.repository.UserRepository;
import com.example.sns.config.JwtProperties;
import com.example.sns.service.auth.JwtService;
import com.example.sns.service.auth.TokenStore;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 인증 서비스 — 로그인·토큰 갱신·로그아웃.
 *
 * RULE 6.1~6.5: JWT, Refresh Token Redis, 블랙리스트.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenStore tokenStore;

    /**
     * 로그인: 이메일·비밀번호 검증 후 Access Token + Refresh Token 발급.
     *
     * @param request 로그인 요청
     * @return LoginResponse (accessToken, expiresIn) + Refresh Token은 쿠키로 설정
     * @throws BusinessException 인증 실패 시 INVALID_CREDENTIALS (401)
     */
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("로그인 실패: 존재하지 않는 이메일, email={}", request.email());
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("로그인 실패: 비밀번호 불일치, email={}", request.email());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        var accessResult = jwtService.createAccessToken(user);
        String refreshJti = jwtService.createRefreshTokenJti();
        long refreshTtlSeconds = (long) jwtProperties.getRefreshTtlDays() * 24 * 60 * 60;
        String refreshPayload = user.getId() + ":" + user.getRole().name();
        tokenStore.saveRefreshToken(refreshJti, refreshPayload, refreshTtlSeconds);

        log.info("로그인 성공: userId={}, email={}", user.getId(), user.getEmail());
        return new LoginResult(
                LoginResponse.of(accessResult.token(), accessResult.expiresInSeconds()),
                refreshJti,
                refreshTtlSeconds
        );
    }

    /**
     * 토큰 갱신: Cookie refreshToken으로 새 Access Token 발급.
     *
     * @param refreshToken Refresh Token (jti 또는 raw token)
     * @return LoginResponse
     * @throws BusinessException Redis에 없거나 유효하지 않으면 401
     */
    public LoginResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("토큰 갱신 실패: refreshToken 없음");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh Token이 필요합니다.");
        }

        Optional<String> payloadOpt = tokenStore.getRefreshToken(refreshToken);
        if (payloadOpt.isEmpty()) {
            log.warn("토큰 갱신 실패: Redis에 refreshToken 없음");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다.");
        }

        String payload = payloadOpt.get();
        String[] parts = payload.split(":");
        if (parts.length != 2) {
            log.warn("토큰 갱신 실패: payload 형식 오류");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다.");
        }

        Long userId = Long.parseLong(parts[0]);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "회원을 찾을 수 없습니다."));

        var accessResult = jwtService.createAccessToken(user);
        log.info("토큰 갱신 성공: userId={}", userId);
        return LoginResponse.of(accessResult.token(), accessResult.expiresInSeconds());
    }

    /**
     * 로그아웃: Access Token jti 블랙리스트 등록, Refresh Token 삭제.
     *
     * @param accessToken Bearer 토큰 (jti 추출용)
     * @param refreshToken Refresh Token jti (Redis 삭제용)
     */
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                Claims claims = jwtService.parseAccessToken(accessToken);
                String jti = claims.getId();
                long remainingSeconds = jwtService.getRemainingSeconds(claims);
                if (jti != null && remainingSeconds > 0) {
                    tokenStore.addToBlacklist(jti, remainingSeconds);
                }
            } catch (JwtException e) {
                log.debug("로그아웃: Access Token 파싱 실패 (이미 만료 등), error={}", e.getMessage());
            }
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenStore.deleteRefreshToken(refreshToken);
        }
        log.info("로그아웃 완료");
    }

    /**
     * 현재 인증된 사용자 조회.
     */
    public Optional<MemberResponse> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return Optional.of(MemberResponse.from(user));
        }
        return Optional.empty();
    }

    public record LoginResult(LoginResponse loginResponse, String refreshToken, long refreshTtlSeconds) {
    }
}
