package com.example.sns.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sns.aop.LogAccess;
import com.example.sns.dto.request.SampleValidationRequest;
import com.example.sns.exception.BusinessException;
import com.example.sns.exception.ErrorCode;

import jakarta.validation.Valid;

/**
 * @Valid 검증 및 예외 처리 예시 Controller.
 *
 *        Step 2 검증·예외 패턴 시연용.
 *        접근 로그는 @LogAccess → AccessLogAspect (RULE 1.4.3).
 */
@RestController
@RequestMapping("/api/sample")
public class SampleController {

    @LogAccess
    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validate(@Valid @RequestBody SampleValidationRequest request) {
        return ResponseEntity.ok(Map.of(
                "result", "success",
                "name", request.getName()));
    }

    @LogAccess
    @PostMapping("/error/business")
    public ResponseEntity<?> triggerBusinessError() {
        throw new BusinessException(ErrorCode.BAD_REQUEST, "테스트용 비즈니스 예외");
    }
}
