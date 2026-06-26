package com.team7.agora.domain.trade.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Trade Complete 요청 본문을 표현하는 DTO이다.
 * @param buyerId 구매자 ID
 */
public record TradeCompleteRequest(
    @NotNull(message = "구매자 id는 필수입니다.")
    Long buyerId
) {
}
