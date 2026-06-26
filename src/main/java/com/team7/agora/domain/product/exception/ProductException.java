package com.team7.agora.domain.product.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

/**
 * Domain exception used for product failures.
 */
public class ProductException extends BusinessException {

    /**
     * Creates a product exception instance.
     * @param errorCode the error code value
     * @param message the message value
     */
    public ProductException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
