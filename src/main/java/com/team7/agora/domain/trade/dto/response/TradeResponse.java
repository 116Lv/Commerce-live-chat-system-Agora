package com.team7.agora.domain.trade.dto.response;

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
 * @param status 입력 값
 * @param completedAt 입력 값
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
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param trade 입력 값
     * @return 처리 결과
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
