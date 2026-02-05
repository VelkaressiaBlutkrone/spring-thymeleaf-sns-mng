package com.example.sns.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 인증 API — 로그인·토큰 갱신·로그아웃·현재 사용자 조회.
 *
 * JWT 기반 (RULE 6.1~6.5). Step 4 API 스텁. 실제 구현은 Step 6.
 */
@Tag(name = "인증 (Auth)", description = "JWT 로그인, 토큰 갱신, 로그아웃, 현재 사용자 조회")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인. 성공 시 Access Token(15분)+Refresh Token(Set-Cookie) 반환")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "토큰 갱신", description = "Cookie refreshToken으로 새 Access Token 발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "로그아웃", description = "jti 블랙리스트 등록, Refresh Token 삭제")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "현재 사용자 조회", description = "Authorization: Bearer {accessToken}")
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 로그인 요청 스텁.
     */
    public record LoginRequest(String email, String password) {
    }
}
