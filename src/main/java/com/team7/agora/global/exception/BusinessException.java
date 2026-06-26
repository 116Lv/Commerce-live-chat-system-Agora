// ErrorCode 기반 비즈니스 예외의 공통 상위 타입
package com.team7.agora.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 도메인 예외이다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param errorCode 입력 값
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param errorCode 입력 값
     * @param message 입력 값
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 데이터를 반환한다.
     * @return 처리 결과
     */
    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}
