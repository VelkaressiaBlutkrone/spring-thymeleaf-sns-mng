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
 * Pin API — CRUD·반경 조회.
 *
 * Step 4 API 스텁. 실제 구현은 Step 10·11.
 */
@Tag(name = "Pin", description = "지도 Pin CRUD, 반경 내 Pin 조회")
@RestController
@RequestMapping("/api/pins")
public class PinController {

    @Operation(summary = "Pin 목록", description = "로그인 사용자 본인 Pin 목록. 페이징")
    @GetMapping
    public ResponseEntity<?> list(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Pin 상세", description = "Pin ID로 상세. 소유자만")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Pin 생성", description = "로그인 필수. 위도·경도·설명")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PinCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Pin 수정", description = "로그인 필수, 소유자만")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PinUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "Pin 삭제", description = "로그인 필수, 소유자만")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "반경 내 Pin 조회", description = "위도·경도·반경(km)으로 주변 Pin 조회. 비로그인 가능")
    @GetMapping("/nearby")
    public ResponseEntity<?> nearby(
            @Parameter(description = "위도") @RequestParam double lat,
            @Parameter(description = "경도") @RequestParam double lng,
            @Parameter(description = "반경(km)") @RequestParam(defaultValue = "5") double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Pin 생성 요청 스텁.
     */
    public record PinCreateRequest(double latitude, double longitude, String description) {
    }

    /**
     * Pin 수정 요청 스텁.
     */
    public record PinUpdateRequest(String description) {
    }
}
