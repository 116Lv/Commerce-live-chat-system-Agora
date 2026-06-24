package com.team7.agora.domain.nego.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

public class NegoException extends BusinessException {

    public NegoException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
