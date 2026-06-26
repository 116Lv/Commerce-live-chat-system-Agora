package com.team7.agora.domain.nego.dto.response;

import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param offerId 입력 값
 * @param chatRoomId 입력 값
 * @param requesterId 입력 값
 * @param offerPrice 입력 값
 * @param status 입력 값
 * @param createdAt 입력 값
 * @param expiresAt 입력 값
 * @param respondedAt 입력 값
 */
public record NegoOfferResponse(
    Long offerId,
    Long tradeId,
    Long chatRoomId,
    Long requesterId,
    BigDecimal offerPrice,
    String status,
    LocalDateTime createdAt,
    LocalDateTime expiresAt,
    LocalDateTime respondedAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param offer 입력 값
     * @return 처리 결과
     */
    public static NegoOfferResponse from(NegoOffer offer) {
        return from(offer, (Long) null);
    }

    public static NegoOfferResponse from(NegoOffer offer, Trade trade) {
        return from(offer, trade == null ? null : trade.getId());
    }

    private static NegoOfferResponse from(NegoOffer offer, Long tradeId) {
        return new NegoOfferResponse(
            offer.getId(),
            tradeId,
            offer.getChatRoom().getId(),
            offer.getRequester().getId(),
            offer.getOfferPrice(),
            offer.getStatus().name(),
            offer.getCreatedAt(),
            offer.getExpiresAt(),
            offer.getRespondedAt()
        );
    }
}
