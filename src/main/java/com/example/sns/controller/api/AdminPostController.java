package com.example.sns.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 관리자 게시물 관리 API — ROLE_ADMIN 전용.
 *
 * Step 4 API 스텁. 실제 구현은 Step 16.
 */
@Tag(name = "관리자 - 게시물 관리", description = "ROLE_ADMIN 전용. 게시글 수정·삭제·공지")
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    @Operation(summary = "게시글 목록", description = "전체 게시글. 페이징·검색")
    @GetMapping
    public ResponseEntity<?> list(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "게시글 수정", description = "관리자 권한으로 타인 글도 수정 가능")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "게시글 삭제", description = "관리자 권한으로 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "공지 등록/해제", description = "게시글을 공지로 지정 또는 해제")
    @PatchMapping("/{id}/notice")
    public ResponseEntity<?> toggleNotice(@PathVariable Long id, @RequestParam boolean notice) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
