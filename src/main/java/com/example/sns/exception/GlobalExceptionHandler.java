package com.example.sns.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/*
 * 전역 예외 처리기.
 * RULE 2.2.3: 모든 예외는 GlobalExceptionHandler에서 일관된 ErrorResponse로 변환.
 * RULE 1.4.1, 2.2.2: 스택 트레이스·내부 경로 사용자 반환 금지.
 * RULE 7.1.2: 공통된 객체 형식으로 반환.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        ErrorResponse response = ErrorResponse.of(e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(err -> ErrorResponse.FieldError.builder()
                        .field(err.getField())
                        .value(err.getRejectedValue() != null ? err.getRejectedValue().toString() : null)
                        .reason(err.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        log.warn("Validation failed: {}", fieldErrors);
        ErrorResponse response = ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(err -> ErrorResponse.FieldError.builder()
                        .field(err.getField())
                        .value(err.getRejectedValue() != null ? err.getRejectedValue().toString() : null)
                        .reason(err.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        log.warn("Bind validation failed: {}", fieldErrors);
        ErrorResponse response = ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getConstraintViolations().stream()
                .map(v -> ErrorResponse.FieldError.builder()
                        .field(v.getPropertyPath().toString())
                        .value(v.getInvalidValue() != null ? v.getInvalidValue().toString() : null)
                        .reason(v.getMessage())
                        .build())
                .collect(Collectors.toList());
        log.warn("Constraint violation: {}", fieldErrors);
        ErrorResponse response = ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getParameterName());
        ErrorResponse response = ErrorResponse.of(ErrorCode.BAD_REQUEST,
                "필수 파라미터가 누락되었습니다: " + e.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: {} for {}", e.getValue(), e.getName());
        ErrorResponse response = ErrorResponse.of(ErrorCode.BAD_REQUEST,
                "파라미터 형식이 올바르지 않습니다: " + e.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.UNAUTHORIZED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.FORBIDDEN);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
