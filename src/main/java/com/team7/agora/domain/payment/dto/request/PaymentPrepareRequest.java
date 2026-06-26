// 결제 준비 API 요청 DTO입니다.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for payment prepare operations.
 * @param tradeId the trade id value
 */
public record PaymentPrepareRequest(
    @NotNull(message = "거래 ID는 필수입니다.")
    Long tradeId
) {
}
