package com.team7.agora.domain.chat.exception;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;

/**
 * Domain exception used for chat failures.
 */
public class ChatException extends BusinessException {

    /**
     * Creates a chat exception instance.
     * @param errorCode the error code value
     * @param message the message value
     */
    public ChatException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
