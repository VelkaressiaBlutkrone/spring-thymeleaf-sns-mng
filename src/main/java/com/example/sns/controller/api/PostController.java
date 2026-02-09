package com.example.sns.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.example.sns.domain.User;
import com.example.sns.dto.request.PostCreateRequest;
import com.example.sns.dto.request.PostUpdateRequest;
import com.example.sns.dto.response.PostResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.service.AuthService;
import com.example.sns.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 게시글 API — 목록·상세·작성·수정·삭제.
 *
 * Step 8: 비로그인 조회 허용, 로그인 필수 작성·수정·삭제, 소유권 검증(IDOR 방지).
 */
@Tag(name = "게시글 (Posts)", description = "게시글 목록, 상세, 작성, 수정, 삭제")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    @Operation(summary = "게시글 목록", description = "페이징·검색. 비로그인 조회 가능")
    @GetMapping
    public ResponseEntity<Page<PostResponse>> list(
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getList(keyword, pageable));
    }

    @Operation(summary = "게시글 상세", description = "게시글 ID로 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @Operation(summary = "게시글 작성", description = "로그인 필수. 제목·내용·위치·Pin 연결")
    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostCreateRequest request) {
        User author = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        PostResponse response = postService.create(request, author);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "게시글 수정", description = "로그인 필수, 작성자만. 403: 타인 글")
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return ResponseEntity.ok(postService.update(id, request, currentUser));
    }

    @Operation(summary = "반경 내 게시글 조회", description = "위도·경도·반경(km)으로 주변 게시글 조회. 비로그인 가능. Step 11")
    @GetMapping("/nearby")
    public ResponseEntity<Page<PostResponse>> nearby(
            @Parameter(description = "위도", required = true) @RequestParam double lat,
            @Parameter(description = "경도", required = true) @RequestParam double lng,
            @Parameter(description = "반경(km)") @RequestParam(defaultValue = "5") double radiusKm,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getNearby(lat, lng, radiusKm, pageable));
    }

    @Operation(summary = "게시글 삭제", description = "로그인 필수, 작성자만. 403: 타인 글")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        postService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
