// Payment prepare request DTO.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;

public record PaymentPrepareRequest(
    @NotNull(message = "Trade ID is required.")
    Long tradeId,
    Long couponId
) {
}
