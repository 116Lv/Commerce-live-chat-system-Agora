package com.team7.agora.domain.trade.dto.request;

import java.util.Locale;

public enum MyTradeRole {
    BUYER,
    SELLER,
    ALL;

    public static MyTradeRole from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        return MyTradeRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}