package com.team7.agora.domain.trade.dto.response;

import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload for returning trade data.
 * @param tradeId the trade id value
 * @param productId the product id value
 * @param sellerId the seller id value
 * @param buyerId the buyer id value
 * @param price the price value
 * @param status the status value
 * @param completedAt the completed at value
 */
public record TradeResponse(
    Long tradeId,
    Long productId,
    Long sellerId,
    Long buyerId,
    BigDecimal price,
    String status,
    LocalDateTime completedAt
) {

    /**
     * Creates a response from the given domain object.
     * @param trade the trade value
     * @return the from result
     */
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
