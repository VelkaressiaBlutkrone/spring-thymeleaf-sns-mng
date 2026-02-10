package com.example.sns.security;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CSP 인라인 스크립트 허용을 위한 nonce 생성.
 * 요청마다 nonce를 생성해 request attribute로 두고, 응답의 Content-Security-Policy 헤더에 있는
 * '{nonce}' 플레이스홀더를 실제 nonce로 치환한다.
 */
public class CspNonceFilter extends OncePerRequestFilter {

    public static final String CSP_NONCE_ATTRIBUTE = "cspNonce";

    private static final String CSP_HEADER = "Content-Security-Policy";
    private static final String NONCE_PLACEHOLDER = "{nonce}";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String nonce = generateNonce();
        request.setAttribute(CSP_NONCE_ATTRIBUTE, nonce);
        CspNonceResponseWrapper wrappedResponse = new CspNonceResponseWrapper(request, response);
        filterChain.doFilter(request, wrappedResponse);
    }

    private static String generateNonce() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).replaceAll("[^a-zA-Z0-9]", "");
    }

    private static final class CspNonceResponseWrapper extends HttpServletResponseWrapper {

        private final HttpServletRequest request;

        CspNonceResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
            super(response);
            this.request = request;
        }

        @Override
        public void setHeader(String name, String value) {
            if (CSP_HEADER.equalsIgnoreCase(name) && value != null && value.contains(NONCE_PLACEHOLDER)) {
                Object nonce = request.getAttribute(CSP_NONCE_ATTRIBUTE);
                value = nonce != null ? value.replace(NONCE_PLACEHOLDER, nonce.toString()) : value;
            }
            super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            if (CSP_HEADER.equalsIgnoreCase(name) && value != null && value.contains(NONCE_PLACEHOLDER)) {
                Object nonce = request.getAttribute(CSP_NONCE_ATTRIBUTE);
                value = nonce != null ? value.replace(NONCE_PLACEHOLDER, nonce.toString()) : value;
            }
            super.addHeader(name, value);
        }
    }
}
