package com.team7.agora.domain.review.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

/**
 * Domain exception used for review failures.
 */
public class ReviewException extends BusinessException {

    /**
     * Creates a review exception instance.
     * @param errorCode the error code value
     * @param message the message value
     */
    public ReviewException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
