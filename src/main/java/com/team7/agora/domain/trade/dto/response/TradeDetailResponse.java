package com.team7.agora.domain.trade.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trade Detail 응답 본문을 표현하는 DTO이다.
 * @param tradeId 거래 ID
 * @param productId 상품 ID
 * @param productTitle 상품명
 * @param sellerId 상품 판매자 ID
 * @param sellerNickname 판매자 닉네임
 * @param buyerId 구매자 ID
 * @param buyerNickname 구매자 닉네임
 * @param price 네고로 결정된 가격
 * @param paidAmount 쿠폰 할인이 반영된 실제 결제 금액
 * @param tradeStatus 거래 진행 상태
 * @param completedAt 거래가 완료된 시각
 * @param paymentId 결제 ID
 * @param paymentStatus 결제 진행 상태
 * @param settlementStatus 정산 진행 상태
 */
public record TradeDetailResponse(
    Long tradeId,
    Long productId,
    String productTitle,
    Long sellerId,
    String sellerNickname,
    Long buyerId,
    String buyerNickname,
    BigDecimal price,
    BigDecimal paidAmount,
    String tradeStatus,
    LocalDateTime completedAt,
    Long paymentId,
    String paymentStatus,
    String settlementStatus
) {

    /**
     * 도메인 객체를 클라이언트 응답 DTO로 변환한다.
     * @param trade 거래 엔티티 또는 거래 응답 변환 대상
     * @param payment 결제 엔티티 또는 결제 응답 변환 대상
     * @param settlement 정산 엔티티 또는 정산 응답 변환 대상
     * @return 클라이언트에 반환할 API 응답
     */
    public static TradeDetailResponse of(Trade trade, Payment payment, Settlement settlement) {
        return new TradeDetailResponse(
            trade.getId(),
            trade.getProduct().getId(),
            trade.getProduct().getTitle(),
            trade.getSeller().getId(),
            trade.getSeller().getNickname(),
            trade.getBuyer().getId(),
            trade.getBuyer().getNickname(),
            trade.getPrice(),
            payment != null ? payment.getAmount() : trade.getPrice(),
            trade.getStatus().name(),
            trade.getCompletedAt(),
            payment != null ? payment.getId() : null,
            payment != null ? payment.getStatus().name() : null,
            settlement != null ? settlement.getStatus().name() : null
        );
    }
}
