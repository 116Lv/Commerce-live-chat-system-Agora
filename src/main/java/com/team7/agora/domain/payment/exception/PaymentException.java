package com.team7.agora.domain.payment.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

/**
 * Domain exception used for payment failures.
 */
public class PaymentException extends BusinessException {

    /**
     * Creates a payment exception instance.
     * @param errorCode the error code value
     * @param message the message value
     */
    public PaymentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
