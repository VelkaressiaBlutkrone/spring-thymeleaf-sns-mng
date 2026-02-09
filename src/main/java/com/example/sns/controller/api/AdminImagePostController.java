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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.sns.aop.AuditLog;
import com.example.sns.dto.response.ImagePostResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.service.ImagePostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 이미지 게시물 관리 API — ROLE_ADMIN 전용.
 *
 * Step 16: 이미지 게시글 목록·수정·삭제·공지 등록/해제.
 * RULE 1.2: ROLE_ADMIN만 접근. RULE 1.4.2: 민감 작업 감사 로그.
 */
@Tag(name = "관리자 - 이미지 게시물 관리", description = "ROLE_ADMIN 전용. 이미지 게시글 수정·삭제·공지")
@RestController
@RequestMapping("/api/admin/image-posts")
@RequiredArgsConstructor
public class AdminImagePostController {

    private final ImagePostService imagePostService;

    @Operation(summary = "이미지 게시글 목록", description = "전체 이미지 게시글. 페이징·검색·공지 상단")
    @GetMapping
    public ResponseEntity<Page<ImagePostResponse>> list(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(imagePostService.getListForAdmin(keyword, pageable));
    }

    @Operation(summary = "이미지 게시글 상세", description = "관리자용 이미지 게시글 상세")
    @GetMapping("/{id}")
    public ResponseEntity<ImagePostResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(imagePostService.getById(id));
    }

    @Operation(summary = "이미지 게시글 수정", description = "관리자 권한으로 타인 글도 수정 가능. multipart/form-data")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @AuditLog("ADMIN_IMAGE_POST_UPDATE")
    public ResponseEntity<ImagePostResponse> update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "제목은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "내용은 필수입니다.");
        }
        return ResponseEntity.ok(imagePostService.updateByAdmin(id, title, content, image));
    }

    @Operation(summary = "이미지 게시글 삭제", description = "관리자 권한으로 삭제")
    @DeleteMapping("/{id}")
    @AuditLog("ADMIN_IMAGE_POST_DELETE")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        imagePostService.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공지 등록/해제", description = "이미지 게시글을 공지로 지정 또는 해제")
    @PatchMapping("/{id}/notice")
    @AuditLog("ADMIN_IMAGE_POST_NOTICE")
    public ResponseEntity<ImagePostResponse> toggleNotice(
            @PathVariable Long id,
            @RequestParam boolean notice) {
        return ResponseEntity.ok(imagePostService.setNotice(id, notice));
    }
}
