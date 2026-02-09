package com.example.sns.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 쿠키 유틸리티.
 *
 * RULE 6.1.6: HttpOnly, Secure, SameSite=Strict.
 */
public final class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final int MAX_AGE_DELETE = 0;
    private static final String SAME_SITE_STRICT = "Strict";

    private CookieUtil() {
    }

    /**
     * Access Token 쿠키 설정. Step 13: 웹 폼 인증용.
     */
    public static void setAccessTokenCookie(HttpServletResponse response, String value, int maxAgeSeconds, boolean secure) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", SAME_SITE_STRICT);
        response.addCookie(cookie);
    }

    /**
     * Refresh Token 쿠키 설정.
     *
     * @param response HttpServletResponse
     * @param value    JTI (refresh token 식별자)
     * @param maxAge   초 단위 TTL
     * @param secure   HTTPS 환경 시 true (local HTTP 시 false)
     */
    public static void setRefreshTokenCookie(HttpServletResponse response, String value, long maxAge, boolean secure) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge((int) Math.min(maxAge, Integer.MAX_VALUE));
        cookie.setAttribute("SameSite", SAME_SITE_STRICT);
        response.addCookie(cookie);
    }

    /**
     * Refresh Token 쿠키 삭제.
     *
     * @param secure HTTPS 환경 시 true (local HTTP 시 false)
     */
    public static void deleteRefreshTokenCookie(HttpServletResponse response, boolean secure) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE_DELETE);
        cookie.setAttribute("SameSite", SAME_SITE_STRICT);
        response.addCookie(cookie);
    }

    /**
     * 요청에서 Refresh Token 쿠키 값 추출.
     */
    public static String getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 요청에서 Bearer 토큰 추출.
     */
    public static String getBearerTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }

    public static String getRefreshTokenCookieName() {
        return REFRESH_TOKEN_COOKIE_NAME;
    }
}
