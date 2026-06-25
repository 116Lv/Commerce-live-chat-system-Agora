package com.team7.agora.domain.product.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

public class ProductException extends BusinessException {

    public ProductException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
