package com.team7.agora.domain.trade.dto.response;

import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 응답 본문을 표현하는 DTO이다.
 * @param tradeId 거래 ID
 * @param productId 상품 ID
 * @param sellerId 상품 판매자 ID
 * @param buyerId 구매자 ID
 * @param price 가격
 * @param status 조회 또는 변경할 상태
 * @param completedAt 거래가 완료된 시각
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
     * @param trade 거래 엔티티 또는 거래 응답 변환 대상
     * @return 클라이언트에 반환할 API 응답
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
