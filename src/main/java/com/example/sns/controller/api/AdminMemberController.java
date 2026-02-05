package com.example.sns.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
 * 관리자 회원 관리 API — ROLE_ADMIN 전용.
 *
 * Step 4 API 스텁. 실제 구현은 Step 15.
 */
@Tag(name = "관리자 - 회원 관리", description = "ROLE_ADMIN 전용. 회원 CRUD")
@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    @Operation(summary = "회원 목록", description = "페이징·검색. ROLE_ADMIN 필수")
    @GetMapping
    public ResponseEntity<?> list(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "이메일/닉네임 검색") @RequestParam(required = false) String keyword) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "회원 상세", description = "관리자용 회원 상세")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "회원 추가", description = "관리자에 의한 회원 등록")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody MemberJoinRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "회원 수정", description = "프로필·역할 등 수정")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Object request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "회원 삭제", description = "회원 탈퇴/삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 회원가입 요청 스텁 (관리자용).
     */
    public record MemberJoinRequest(String email, String password, String nickname) {
    }
}
