package com.team7.agora.domain.trade.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload for returning trade detail data.
 * @param tradeId the trade id value
 * @param productId the product id value
 * @param sellerId the seller id value
 * @param buyerId the buyer id value
 * @param price the price value
 * @param tradeStatus the trade status value
 * @param completedAt the completed at value
 * @param paymentStatus the payment status value
 * @param settlementStatus the settlement status value
 */
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

    /**
     * Handles of behavior.
     * @param trade the trade value
     * @param payment the payment value
     * @param settlement the settlement value
     * @return the of result
     */
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
