package com.example.sns.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sns.aop.AuditLog;
import com.example.sns.aop.ValidCheck;
import com.example.sns.dto.request.PostUpdateRequest;
import com.example.sns.dto.response.PostResponse;
import com.example.sns.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 게시물 관리 API — ROLE_ADMIN 전용.
 *
 * Step 16: 게시글 목록·수정·삭제·공지 등록/해제.
 * RULE 1.2: ROLE_ADMIN만 접근. RULE 1.4.2: 민감 작업 감사 로그.
 */
@Tag(name = "관리자 - 게시물 관리", description = "ROLE_ADMIN 전용. 게시글 수정·삭제·공지")
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록", description = "전체 게시글. 페이징·검색·공지 상단")
    @GetMapping
    public ResponseEntity<Page<PostResponse>> list(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getListForAdmin(keyword, pageable));
    }

    @Operation(summary = "게시글 상세", description = "관리자용 게시글 상세")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @Operation(summary = "게시글 수정", description = "관리자 권한으로 타인 글도 수정 가능")
    @PutMapping("/{id}")
    @ValidCheck
    @AuditLog("ADMIN_POST_UPDATE")
    public ResponseEntity<PostResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(postService.updateByAdmin(id, request));
    }

    @Operation(summary = "게시글 삭제", description = "관리자 권한으로 삭제")
    @DeleteMapping("/{id}")
    @AuditLog("ADMIN_POST_DELETE")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공지 등록/해제", description = "게시글을 공지로 지정 또는 해제")
    @PatchMapping("/{id}/notice")
    @AuditLog("ADMIN_POST_NOTICE")
    public ResponseEntity<PostResponse> toggleNotice(
            @PathVariable Long id,
            @RequestParam boolean notice) {
        return ResponseEntity.ok(postService.setNotice(id, notice));
    }
}
