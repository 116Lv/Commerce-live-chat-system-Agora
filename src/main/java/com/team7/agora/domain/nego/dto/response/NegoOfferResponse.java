package com.team7.agora.domain.nego.dto.response;

import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Nego Offer 응답 본문을 표현하는 DTO이다.
 * @param offerId 네고 제안 ID
 * @param chatRoomId 채팅방 ID
 * @param requesterId 가격 제안을 생성한 구매자 ID
 * @param offerPrice 제안 가격
 * @param status 조회 또는 변경할 상태
 * @param createdAt 데이터가 생성된 시각
 * @param expiresAt 토큰 또는 제안이 만료되는 시각
 * @param respondedAt 판매자가 제안에 응답한 시각
 */
public record NegoOfferResponse(
    Long offerId,
    Long tradeId,
    Long chatRoomId,
    Long requesterId,
    BigDecimal offerPrice,
    String status,
    String tradeStatus,
    String paymentStatus,
    LocalDateTime createdAt,
    LocalDateTime expiresAt,
    LocalDateTime respondedAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param offer 네고 제안 엔티티
     * @return 클라이언트에 반환할 API 응답
     */
    public static NegoOfferResponse from(NegoOffer offer) {
        return from(offer, (Long) null);
    }

    public static NegoOfferResponse from(NegoOffer offer, Trade trade) {
        return from(offer, trade, null);
    }

    public static NegoOfferResponse from(NegoOffer offer, Trade trade, Payment payment) {
        return from(
            offer,
            trade == null ? null : trade.getId(),
            trade == null ? null : trade.getStatus().name(),
            payment == null ? null : payment.getStatus().name()
        );
    }

    private static NegoOfferResponse from(NegoOffer offer, Long tradeId) {
        return from(offer, tradeId, null, null);
    }

    private static NegoOfferResponse from(NegoOffer offer, Long tradeId, String tradeStatus, String paymentStatus) {
        return new NegoOfferResponse(
            offer.getId(),
            tradeId,
            offer.getChatRoom().getId(),
            offer.getRequester().getId(),
            offer.getOfferPrice(),
            offer.getStatus().name(),
            tradeStatus,
            paymentStatus,
            offer.getCreatedAt(),
            offer.getExpiresAt(),
            offer.getRespondedAt()
        );
    }
}
