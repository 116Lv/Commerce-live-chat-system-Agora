package com.team7.agora.domain.trade.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

/**
 * 도메인 예외이다.
 */
public class TradeException extends BusinessException {

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param errorCode 입력 값
     * @param message 입력 값
     */
    public TradeException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
