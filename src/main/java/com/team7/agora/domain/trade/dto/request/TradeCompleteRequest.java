package com.team7.agora.domain.trade.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for trade complete operations.
 * @param buyerId the buyer id value
 */
public record TradeCompleteRequest(
    @NotNull(message = "구매자 id는 필수입니다.")
    Long buyerId
) {
}
