package com.team7.agora.domain.trade.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

public class TradeException extends BusinessException {

    public TradeException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
