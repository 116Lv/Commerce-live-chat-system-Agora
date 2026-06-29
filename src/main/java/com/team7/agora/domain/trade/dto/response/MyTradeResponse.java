package com.team7.agora.domain.trade.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyTradeResponse(
    Long tradeId,
    Long productId,
    String productTitle,
    Long productPrice,
    Long tradePrice,
    String status,
    String role,
    String counterpartNickname,
    String paymentStatus,
    LocalDateTime completedAt,
    LocalDateTime createdAt
) {

    public MyTradeResponse(
        Long tradeId,
        Long productId,
        String productTitle,
        BigDecimal productPrice,
        BigDecimal tradePrice,
        String status,
        String role,
        String counterpartNickname,
        String paymentStatus,
        LocalDateTime completedAt,
        LocalDateTime createdAt
    ) {
        this(
            tradeId,
            productId,
            productTitle,
            toLong(productPrice),
            toLong(tradePrice),
            status,
            role,
            counterpartNickname,
            paymentStatus,
            completedAt,
            createdAt
        );
    }

    private static Long toLong(BigDecimal value) {
        return value == null ? null : value.longValue();
    }
}