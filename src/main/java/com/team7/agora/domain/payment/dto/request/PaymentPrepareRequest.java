// 결제 준비 API 요청 DTO입니다.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Payment Prepare 요청 본문을 표현하는 DTO이다.
 * @param tradeId 거래 ID
 */
public record PaymentPrepareRequest(
    @NotNull(message = "거래 ID는 필수입니다.")
    Long tradeId
) {
}
