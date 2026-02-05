package com.example.sns.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 회원 API — 가입·조회.
 *
 * Step 4 API 스텁. 실제 구현은 Step 6.
 */
@Tag(name = "회원 (Members)", description = "회원가입, 회원 조회")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Operation(summary = "회원가입", description = "이메일·비밀번호·닉네임으로 가입")
    @PostMapping
    public ResponseEntity<?> join(@Valid @RequestBody MemberJoinRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "회원 조회", description = "회원 ID로 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<?> getMember(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 회원가입 요청 스텁.
     */
    public record MemberJoinRequest(String email, String password, String nickname) {
    }
}
