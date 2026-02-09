package com.example.sns.controller.api;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.sns.domain.User;
import com.example.sns.dto.response.ImagePostResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.service.AuthService;
import com.example.sns.service.ImagePostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 이미지 게시글 API — 목록·상세·Multipart 업로드·수정·삭제.
 *
 * Step 9: Multipart 검증·저장, Post와 동일 권한(작성자만 수정/삭제).
 */
@Tag(name = "이미지 게시글 (ImagePosts)", description = "이미지+텍스트 게시글 CRUD")
@RestController
@RequestMapping("/api/image-posts")
@RequiredArgsConstructor
public class ImagePostController {

    private final ImagePostService imagePostService;
    private final AuthService authService;

    @Operation(summary = "이미지 게시글 목록", description = "페이징·검색. 비로그인 조회 가능")
    @GetMapping
    public ResponseEntity<Page<ImagePostResponse>> list(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(imagePostService.getList(keyword, pageable));
    }

    @Operation(summary = "반경 내 이미지 게시글 조회", description = "위도·경도·반경(km)으로 주변 이미지 게시글 조회. 비로그인 가능. Step 11")
    @GetMapping("/nearby")
    public ResponseEntity<Page<ImagePostResponse>> nearby(
            @Parameter(description = "위도", required = true) @RequestParam double lat,
            @Parameter(description = "경도", required = true) @RequestParam double lng,
            @Parameter(description = "반경(km)") @RequestParam(defaultValue = "5") double radiusKm,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(imagePostService.getNearby(lat, lng, radiusKm, pageable));
    }

    @Operation(summary = "이미지 게시글 상세", description = "ID로 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ImagePostResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(imagePostService.getById(id));
    }

    @Operation(summary = "이미지 파일 조회", description = "이미지 게시글의 이미지 바이너리 반환")
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        Resource resource = imagePostService.getImageResource(id);
        String contentType = "image/jpeg";
        if (resource.getFilename() != null) {
            String fn = resource.getFilename().toLowerCase();
            if (fn.endsWith(".png")) {
                contentType = "image/png";
            } else if (fn.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fn.endsWith(".webp")) {
                contentType = "image/webp";
            }
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + (resource.getFilename() != null ? resource.getFilename() : "image") + "\"")
                .body(resource);
    }

    @Operation(summary = "이미지 게시글 작성", description = "Multipart 업로드. 로그인 필수")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagePostResponse> create(
            @Parameter(description = "제목") @RequestParam String title,
            @Parameter(description = "내용") @RequestParam String content,
            @Parameter(description = "이미지 파일") @RequestParam MultipartFile image,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Long pinId) {
        validateCreateParams(title, content, image);

        User author = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        ImagePostResponse response = imagePostService.create(title, content, image, latitude, longitude, pinId, author);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "이미지 게시글 수정", description = "로그인 필수, 작성자만. multipart/form-data")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        return ResponseEntity.ok(imagePostService.update(id, title, content, image, currentUser));
    }

    @Operation(summary = "이미지 게시글 삭제", description = "로그인 필수, 작성자만")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        imagePostService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    private void validateCreateParams(String title, String content, MultipartFile image) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "제목은 필수입니다.");
        }
        if (title.length() > 200) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "제목은 200자 이하여야 합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "내용은 필수입니다.");
        }
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이미지 파일이 필요합니다.");
        }
    }
}
