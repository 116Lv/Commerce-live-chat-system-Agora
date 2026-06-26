package com.team7.agora.domain.trade.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

/**
 * 도메인 예외이다.
 */
public class TradeException extends BusinessException {

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param errorCode 비즈니스 예외에 사용할 에러 코드
     * @param message 메시지
     */
    public TradeException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
