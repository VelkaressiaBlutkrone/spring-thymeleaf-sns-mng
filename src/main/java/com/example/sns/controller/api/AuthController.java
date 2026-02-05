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
 * 인증 API — 로그인·로그아웃·현재 사용자 조회.
 *
 * Step 4 API 스텁. 실제 구현은 Step 6.
 */
@Tag(name = "인증 (Auth)", description = "로그인, 로그아웃, 현재 사용자 조회")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인. 성공 시 세션 생성(Redis), Set-Cookie")
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "로그아웃", description = "세션 무효화(Redis 삭제)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "현재 사용자 조회", description = "로그인 필수. 세션 기반 사용자 정보")
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
