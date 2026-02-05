package com.example.sns.exception;

import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드 정의.
 *
 * 비즈니스 로직 예외는 반드시 ErrorCode enum 사용 (RULE 2.2.3).
 */
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "E001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "E003", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E004", "요청한 리소스를 찾을 수 없습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "E005", "입력값 검증에 실패했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "E503", "일시적으로 서비스를 이용할 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E999", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
