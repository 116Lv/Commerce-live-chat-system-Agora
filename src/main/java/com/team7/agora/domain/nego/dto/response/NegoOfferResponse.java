package com.team7.agora.domain.nego.dto.response;

import com.team7.agora.domain.nego.entity.NegoOffer;
import com.team7.agora.domain.trade.entity.Trade;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
