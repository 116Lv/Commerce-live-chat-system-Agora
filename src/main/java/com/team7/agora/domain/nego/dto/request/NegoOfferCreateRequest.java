package com.team7.agora.domain.nego.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Nego Offer Create 요청 본문을 표현하는 DTO이다.
 * @param offerPrice 제안 가격
 */
public record NegoOfferCreateRequest(
    @NotNull(message = "제안 가격은 필수입니다.")
    @Positive(message = "제안 가격은 0보다 커야 합니다.")
    BigDecimal offerPrice
) {
}
