package com.team7.agora.domain.nego.dto.response;

import com.team7.agora.domain.nego.entity.NegoOffer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NegoOfferResponse(
    Long offerId,
    Long chatRoomId,
    Long requesterId,
    BigDecimal offerPrice,
    String status,
    LocalDateTime createdAt,
    LocalDateTime expiresAt,
    LocalDateTime respondedAt
) {

    public static NegoOfferResponse from(NegoOffer offer) {
        return new NegoOfferResponse(
            offer.getId(),
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
