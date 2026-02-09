package com.example.sns.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sns.aop.ValidCheck;
import com.example.sns.dto.request.MemberJoinRequest;
import com.example.sns.dto.response.MemberResponse;
import com.example.sns.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 회원 API — 가입·조회.
 */
@Tag(name = "회원 (Members)", description = "회원가입, 회원 조회")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "이메일·비밀번호·닉네임으로 가입")
    @PostMapping
    @ValidCheck
    public ResponseEntity<MemberResponse> join(@Valid @RequestBody MemberJoinRequest request) {
        MemberResponse response = memberService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "회원 조회", description = "회원 ID로 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long id) {
        MemberResponse response = memberService.getById(id);
        return ResponseEntity.ok(response);
    }
}
