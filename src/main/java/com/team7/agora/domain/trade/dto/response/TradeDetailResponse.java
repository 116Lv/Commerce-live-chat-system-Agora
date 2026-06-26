package com.team7.agora.domain.trade.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param tradeId 입력 값
 * @param productId 입력 값
 * @param sellerId 입력 값
 * @param buyerId 입력 값
 * @param price 입력 값
 * @param tradeStatus 입력 값
 * @param completedAt 입력 값
 * @param paymentStatus 입력 값
 * @param settlementStatus 입력 값
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
     * 요청한 동작을 처리한다.
     * @param trade 입력 값
     * @param payment 입력 값
     * @param settlement 입력 값
     * @return 처리 결과
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
