package com.example.sns.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.sns.security.CspNonceFilter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * CSP nonce를 뷰 모델에 넣어 인라인 스크립트에 nonce 속성을 부여할 수 있게 함.
 */
@ControllerAdvice
public class CspNonceAdvice {

    @ModelAttribute("cspNonce")
    public Object cspNonce(HttpServletRequest request) {
        return request.getAttribute(CspNonceFilter.CSP_NONCE_ATTRIBUTE);
    }
}
