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
import com.example.sns.dto.request.PinCreateRequest;
import com.example.sns.dto.request.PinUpdateRequest;
import com.example.sns.dto.response.PinResponse;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.service.AuthService;
import com.example.sns.service.PinService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Pin API — CRUD.
 *
 * Step 10: Pin 생성/수정/삭제/목록(사용자별), 소유권 검증.
 * Step 11: 반경 조회(nearby) 구현 예정.
 */
@Tag(name = "Pin", description = "지도 Pin CRUD, 반경 내 Pin 조회")
@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinController {

    private final PinService pinService;
    private final AuthService authService;

    @Operation(summary = "Pin 목록", description = "로그인 사용자 본인 Pin 목록. 페이징")
    @GetMapping
    public ResponseEntity<Page<PinResponse>> list(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(pinService.getListByOwner(currentUser, pageable));
    }

    @Operation(summary = "Pin 상세", description = "Pin ID로 상세. 소유자만")
    @GetMapping("/{id}")
    public ResponseEntity<PinResponse> get(@PathVariable Long id) {
        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return ResponseEntity.ok(pinService.getById(id, currentUser));
    }

    @Operation(summary = "Pin 생성", description = "로그인 필수. 위도·경도·설명")
    @PostMapping
    public ResponseEntity<PinResponse> create(@Valid @RequestBody PinCreateRequest request) {
        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        PinResponse response = pinService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Pin 수정", description = "로그인 필수, 소유자만")
    @PutMapping("/{id}")
    public ResponseEntity<PinResponse> update(@PathVariable Long id, @Valid @RequestBody PinUpdateRequest request) {
        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return ResponseEntity.ok(pinService.update(id, request, currentUser));
    }

    @Operation(summary = "Pin 삭제", description = "로그인 필수, 소유자만")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = authService.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        pinService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "반경 내 Pin 조회", description = "위도·경도·반경(km)으로 주변 Pin 조회. 비로그인 가능. Step 11에서 구현")
    @GetMapping("/nearby")
    public ResponseEntity<?> nearby(
            @Parameter(description = "위도") @RequestParam double lat,
            @Parameter(description = "경도") @RequestParam double lng,
            @Parameter(description = "반경(km)") @RequestParam(defaultValue = "5") double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
