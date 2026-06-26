package com.team7.agora.domain.nego.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request payload for nego offer create operations.
 * @param offerPrice the offer price value
 */
public record NegoOfferCreateRequest(
    @NotNull(message = "제안 가격은 필수입니다.")
    @Positive(message = "제안 가격은 0보다 커야 합니다.")
    BigDecimal offerPrice
) {
}
