// 결제 준비 API 요청 DTO입니다.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;

public record PaymentPrepareRequest(
    @NotNull(message = "거래 ID는 필수입니다.")
    Long tradeId
) {
}
