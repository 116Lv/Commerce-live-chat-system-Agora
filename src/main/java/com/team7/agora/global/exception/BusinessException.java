// ErrorCode 기반 비즈니스 예외의 공통 상위 타입
package com.team7.agora.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Domain exception used for business failures.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Creates a business exception instance.
     * @param errorCode the error code value
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Creates a business exception instance.
     * @param errorCode the error code value
     * @param message the message value
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns status data.
     * @return the get status result
     */
    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}
