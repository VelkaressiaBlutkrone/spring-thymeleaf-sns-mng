package com.example.sns.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.sns.repository.UserRepository;
import com.example.sns.service.auth.JwtService;
import com.example.sns.service.auth.TokenStore;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 인증 필터.
 *
 * Bearer 토큰 검증 후 SecurityContext에 인증 정보 설정.
 * RULE 6.1: 모든 JWT 검증, jti 블랙리스트 확인.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenStore tokenStore;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractBearerToken(request);
            if (StringUtils.hasText(token)) {
                Claims claims = jwtService.parseAccessToken(token);
                String jti = claims.getId();
                if (jti != null && tokenStore.isBlacklisted(jti)) {
                    log.debug("블랙리스트된 토큰: jti={}", jti);
                } else {
                    Long userId = claims.get(CLAIM_USER_ID, Long.class);
                    if (userId != null) {
                        userRepository.findById(userId).ifPresent(user -> {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    user, null,
                                    Collections
                                            .singletonList(new SimpleGrantedAuthority(user.getRole().toAuthority())));
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
                    }
                }
            }
        } catch (JwtException e) {
            log.debug("JWT 검증 실패: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        // Step 13: 웹 폼 요청용. 쿠키에서 access_token 읽기 (SameSite=Strict 권장)
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie c : cookies) {
                if ("access_token".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    return c.getValue().trim();
                }
            }
        }
        return null;
    }

    private static final String CLAIM_USER_ID = "userId";
}
