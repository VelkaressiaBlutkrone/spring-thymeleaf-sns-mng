package com.example.sns.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 관리자 통계 API — ROLE_ADMIN 전용.
 *
 * Step 4 API 스텁. 실제 구현은 Step 17.
 */
@Tag(name = "관리자 - 통계", description = "ROLE_ADMIN 전용. 가입·로그인·글 통계")
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    @Operation(summary = "가입 통계", description = "기간별 가입자 수")
    @GetMapping("/signup")
    public ResponseEntity<?> signup(
            @Parameter(description = "시작일 (yyyy-MM-dd)") @RequestParam String startDate,
            @Parameter(description = "종료일 (yyyy-MM-dd)") @RequestParam String endDate) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "로그인 통계", description = "기간별 로그인 수·활성 사용자")
    @GetMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Operation(summary = "글 통계", description = "일/주/월/분기/년 단위 게시글 수")
    @GetMapping("/posts")
    public ResponseEntity<?> posts(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @Parameter(description = "단위: day, week, month, quarter, year") @RequestParam(defaultValue = "day") String unit) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
