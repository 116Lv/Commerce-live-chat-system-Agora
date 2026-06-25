package com.team7.agora.domain.trade.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeDetailResponse(
    Long tradeId,
    Long productId,
    Long sellerId,
    Long buyerId,
    BigDecimal price,
    String tradeStatus,
    LocalDateTime completedAt,
    String paymentStatus,
    String settlementStatus
) {

    public static TradeDetailResponse of(Trade trade, Payment payment, Settlement settlement) {
        return new TradeDetailResponse(
            trade.getId(),
            trade.getProduct().getId(),
            trade.getSeller().getId(),
            trade.getBuyer().getId(),
            trade.getPrice(),
            trade.getStatus().name(),
            trade.getCompletedAt(),
            payment != null ? payment.getStatus().name() : null,
            settlement != null ? settlement.getStatus().name() : null
        );
    }
}
