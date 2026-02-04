package com.example.sns.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외.
 * RULE 2.2.3: 비즈니스 로직 예외는 반드시 BusinessException 사용.
 * RULE 2.2.4: IllegalArgumentException, IllegalStateException 직접 사용 금지.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getDefaultMessage();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message != null ? message : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.message = message != null ? message : errorCode.getDefaultMessage();
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.message = errorCode.getDefaultMessage();
    }
}
