package com.team7.agora.domain.payment.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

public class PaymentException extends BusinessException {

    public PaymentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
