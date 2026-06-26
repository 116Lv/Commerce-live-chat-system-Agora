package com.team7.agora.domain.nego.dto.response;

import com.team7.agora.domain.nego.entity.NegoOffer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload for returning nego offer data.
 * @param offerId the offer id value
 * @param chatRoomId the chat room id value
 * @param requesterId the requester id value
 * @param offerPrice the offer price value
 * @param status the status value
 * @param createdAt the created at value
 * @param expiresAt the expires at value
 * @param respondedAt the responded at value
 */
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

    /**
     * Creates a response from the given domain object.
     * @param offer the offer value
     * @return the from result
     */
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
