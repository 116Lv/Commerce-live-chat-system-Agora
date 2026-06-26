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
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param errorCode 비즈니스 예외에 사용할 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param errorCode 비즈니스 예외에 사용할 에러 코드
     * @param message 메시지
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 'getStatus' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @return 클라이언트에 반환할 API 응답
     */
    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}
