package com.team7.agora.domain.review.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

public class ReviewException extends BusinessException {

    public ReviewException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
