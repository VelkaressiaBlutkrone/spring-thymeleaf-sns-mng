package com.example.sns.controller.api;

import org.springframework.http.HttpStatus;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 이미지 게시글 API — 목록·상세·Multipart 업로드·수정·삭제.
 *
 * Step 4 API 스텁. 실제 구현은 Step 9.
 */
@Tag(name = "이미지 게시글 (ImagePosts)", description = "이미지+텍스트 게시글 CRUD")
@RestController
@RequestMapping("/api/image-posts")
public class ImagePostController {

    @Operation(summary = "이미지 게시글 목록", description = "페이징·검색. 비로그인 조회 가능")
    @GetMapping
    public ResponseEntity<?> list(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "이미지 게시글 상세", description = "ID로 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "이미지 게시글 작성", description = "Multipart 업로드. 로그인 필수")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> create(
            @Parameter(description = "제목") @RequestParam String title,
            @Parameter(description = "내용") @RequestParam String content,
            @Parameter(description = "이미지 파일") @RequestParam MultipartFile image,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Long pinId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "이미지 게시글 수정", description = "로그인 필수, 작성자만")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "이미지 게시글 삭제", description = "로그인 필수, 작성자만")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
