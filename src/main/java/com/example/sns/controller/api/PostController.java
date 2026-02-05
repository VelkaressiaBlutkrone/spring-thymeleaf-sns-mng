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
 * 게시글 API — 목록·상세·작성·수정·삭제.
 *
 * Step 4 API 스텁. 실제 구현은 Step 8.
 */
@Tag(name = "게시글 (Posts)", description = "게시글 목록, 상세, 작성, 수정, 삭제")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Operation(summary = "게시글 목록", description = "페이징·검색. 비로그인 조회 가능")
    @GetMapping
    public ResponseEntity<?> list(
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "게시글 상세", description = "게시글 ID로 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "게시글 작성", description = "로그인 필수. 제목·내용·위치·Pin 연결")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "게시글 수정", description = "로그인 필수, 작성자만. 403: 타인 글")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "게시글 삭제", description = "로그인 필수, 작성자만. 403: 타인 글")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 게시글 작성 요청 스텁.
     */
    public record PostCreateRequest(String title, String content, Double latitude, Double longitude, Long pinId) {
    }

    /**
     * 게시글 수정 요청 스텁.
     */
    public record PostUpdateRequest(String title, String content) {
    }
}
