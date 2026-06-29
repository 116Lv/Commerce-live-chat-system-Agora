package com.team7.agora.domain.trade.dto.request;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Locale;

public enum MyTradeRole {
    BUYER,
    SELLER,
    ALL;

    public static MyTradeRole from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return MyTradeRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "role must be one of buyer, seller, or all.");
        }
    }
}
