package com.team7.agora.domain.trade.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

/**
 * Domain exception used for trade failures.
 */
public class TradeException extends BusinessException {

    /**
     * Creates a trade exception instance.
     * @param errorCode the error code value
     * @param message the message value
     */
    public TradeException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
