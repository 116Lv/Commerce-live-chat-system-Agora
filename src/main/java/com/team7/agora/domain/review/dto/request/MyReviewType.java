package com.team7.agora.domain.review.dto.request;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Locale;

public enum MyReviewType {
    WRITTEN,
    RECEIVED;

    public static MyReviewType from(String value) {
        if (value == null || value.isBlank()) {
            return WRITTEN;
        }
        try {
            return MyReviewType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "type must be one of written or received.");
        }
    }
}
