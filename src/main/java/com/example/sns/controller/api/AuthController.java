package com.example.sns.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sns.dto.request.LoginRequest;
import com.example.sns.dto.response.LoginResponse;
import com.example.sns.dto.response.MemberResponse;
import com.example.sns.service.AuthService;
import com.example.sns.util.CookieUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;

/**
 * 인증 API — 로그인·토큰 갱신·로그아웃·현재 사용자 조회.
 *
 * JWT 기반 (RULE 6.1~6.5).
 */
@Tag(name = "인증 (Auth)", description = "JWT 로그인, 토큰 갱신, 로그아웃, 현재 사용자 조회")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인. 성공 시 Access Token(15분)+Refresh Token(Set-Cookie) 반환")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        var result = authService.login(request);
        CookieUtil.setRefreshTokenCookie(response, result.refreshToken(), result.refreshTtlSeconds(), cookieSecure);
        return ResponseEntity.ok(result.loginResponse());
    }

    @Operation(summary = "토큰 갱신", description = "Cookie refreshToken으로 새 Access Token 발급")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        String refreshToken = CookieUtil.getRefreshTokenFromRequest(request);
        LoginResponse loginResponse = authService.refresh(refreshToken);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "로그아웃", description = "jti 블랙리스트 등록, Refresh Token 삭제")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = CookieUtil.getBearerTokenFromRequest(request);
        String refreshToken = CookieUtil.getRefreshTokenFromRequest(request);
        authService.logout(accessToken, refreshToken);
        CookieUtil.deleteRefreshTokenCookie(response, cookieSecure);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "현재 사용자 조회", description = "Authorization: Bearer {accessToken}")
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me() {
        return authService.getCurrentUser()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}
