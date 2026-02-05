package com.example.sns.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 마이페이지 API — 내 게시글·Pin·개인정보 수정.
 *
 * Step 4 API 스텁. 실제 구현은 Step 14.
 */
@Tag(name = "마이페이지 (Me)", description = "내 게시글, 이미지 게시글, Pin 목록, 개인정보 수정")
@RestController
@RequestMapping("/api/me")
public class MeController {

    @Operation(summary = "내 게시글 목록", description = "로그인 필수. 본인 게시글만")
    @GetMapping("/posts")
    public ResponseEntity<?> myPosts(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "내 이미지 게시글 목록", description = "로그인 필수")
    @GetMapping("/image-posts")
    public ResponseEntity<?> myImagePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "내 Pin 목록", description = "로그인 필수")
    @GetMapping("/pins")
    public ResponseEntity<?> myPins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "개인정보 수정", description = "로그인 필수. 닉네임 등")
    @PutMapping
    public ResponseEntity<?> updateMe(@Valid @RequestBody MemberUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 회원 수정 요청 스텁.
     */
    public record MemberUpdateRequest(String nickname) {
    }
}
