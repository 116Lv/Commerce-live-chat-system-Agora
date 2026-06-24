package com.team7.agora.domain.trade.dto.response;

import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse(
    Long tradeId,
    Long productId,
    Long sellerId,
    Long buyerId,
    BigDecimal price,
    String status,
    LocalDateTime completedAt
) {

    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
            trade.getId(),
            trade.getProduct().getId(),
            trade.getSeller().getId(),
            trade.getBuyer().getId(),
            trade.getPrice(),
            trade.getStatus().name(),
            trade.getCompletedAt()
        );
    }
}
