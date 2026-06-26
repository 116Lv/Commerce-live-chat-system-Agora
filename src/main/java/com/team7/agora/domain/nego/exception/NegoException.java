package com.team7.agora.domain.nego.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

/**
 * Domain exception used for nego failures.
 */
public class NegoException extends BusinessException {

    /**
     * Creates a nego exception instance.
     * @param errorCode the error code value
     * @param message the message value
     */
    public NegoException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
