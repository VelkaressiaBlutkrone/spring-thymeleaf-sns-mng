package com.example.sns.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sns.domain.UserRole;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;
import com.example.sns.service.AdminStatsService;
import com.example.sns.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 통계 뷰. Step 17.
 *
 * GET /admin/stats — 기간별 가입·로그인·글 통계 (테이블).
 * RULE 1.2: ROLE_ADMIN만 접근.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminStatsViewController {

    private static final int DEFAULT_DAYS = 30;

    private final AuthService authService;
    private final AdminStatsService adminStatsService;

    /**
     * 통계 페이지. 기간 미지정 시 최근 30일.
     *
     * @param startDate 시작일 (yyyy-MM-dd), 없으면 endDate - 30일
     * @param endDate   종료일 (yyyy-MM-dd), 없으면 오늘
     */
    @GetMapping("/stats")
    public String stats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        ensureAdmin();

        LocalDate end = parseDate(endDate).orElse(LocalDate.now());
        LocalDate start = parseDate(startDate).orElse(end.minusDays(DEFAULT_DAYS));
        if (start.isAfter(end)) {
            start = end.minusDays(DEFAULT_DAYS);
        }

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("signupStats", adminStatsService.getSignupStats(start, end));
        model.addAttribute("loginStats", adminStatsService.getLoginStats(start, end));
        model.addAttribute("postStats", adminStatsService.getPostStats(start, end, "day"));
        return "admin/stats";
    }

    private static Optional<LocalDate> parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private void ensureAdmin() {
        authService.getCurrentUserEntity()
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "관리자만 접근할 수 있습니다."));
    }
}
