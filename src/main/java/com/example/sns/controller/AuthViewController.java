package com.example.sns.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sns.dto.request.LoginRequest;
import com.example.sns.exception.BusinessException;
import com.example.sns.service.AuthService;
import com.example.sns.util.CookieUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 웹 인증 뷰 (로그인 폼).
 *
 * Step 13: 웹 폼 기반 게시글 작성 시 쿠키 인증용 로그인 페이지.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        try {
            var result = authService.login(new LoginRequest(email, password));
            CookieUtil.setAccessTokenCookie(response, result.loginResponse().accessToken(),
                    (int) result.loginResponse().expiresIn(), cookieSecure);
            CookieUtil.setRefreshTokenCookie(response, result.refreshToken(), result.refreshTtlSeconds(), cookieSecure);
            return "redirect:/";
        } catch (BusinessException e) {
            log.warn("웹 로그인 실패: email={}", email);
            redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/login";
        }
    }
}
