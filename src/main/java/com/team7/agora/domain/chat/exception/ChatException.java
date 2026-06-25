package com.team7.agora.domain.chat.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

public class ChatException extends BusinessException {

    public ChatException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
