package com.example.sns.controller.api;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sns.dto.response.LoginStatsResponse;
import com.example.sns.dto.response.PostStatsResponse;
import com.example.sns.dto.response.SignupStatsResponse;
import com.example.sns.service.AdminStatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 통계 API — ROLE_ADMIN 전용. Step 17.
 */
@Tag(name = "관리자 - 통계", description = "ROLE_ADMIN 전용. 가입·로그인·글 통계")
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @Operation(summary = "가입 통계", description = "기간별 가입자 수")
    @GetMapping("/signup")
    public ResponseEntity<SignupStatsResponse> signup(
            @Parameter(description = "시작일 (yyyy-MM-dd)") @RequestParam String startDate,
            @Parameter(description = "종료일 (yyyy-MM-dd)") @RequestParam String endDate) {
        var range = parseRange(startDate, endDate);
        if (range == null) {
            return ResponseEntity.badRequest().build();
        }
        SignupStatsResponse body = adminStatsService.getSignupStats(range.start(), range.end());
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "로그인 통계", description = "기간별 로그인 수·활성 사용자")
    @GetMapping("/login")
    public ResponseEntity<LoginStatsResponse> login(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        var range = parseRange(startDate, endDate);
        if (range == null) {
            return ResponseEntity.badRequest().build();
        }
        LoginStatsResponse body = adminStatsService.getLoginStats(range.start(), range.end());
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "글 통계", description = "일/주/월/분기/년 단위 게시글 수")
    @GetMapping("/posts")
    public ResponseEntity<PostStatsResponse> posts(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @Parameter(description = "단위: day, week, month, quarter, year") @RequestParam(defaultValue = "day") String unit) {
        var range = parseRange(startDate, endDate);
        if (range == null) {
            return ResponseEntity.badRequest().build();
        }
        PostStatsResponse body = adminStatsService.getPostStats(range.start(), range.end(), unit);
        return ResponseEntity.ok(body);
    }

    private record DateRange(LocalDate start, LocalDate end) {}

    private static DateRange parseRange(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            if (start.isAfter(end)) {
                return null;
            }
            return new DateRange(start, end);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
