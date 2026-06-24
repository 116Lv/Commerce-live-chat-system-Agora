// 결제 승인 API 요청 DTO입니다.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentConfirmByIdRequest(
    @NotNull(message = "결제 ID는 필수입니다.")
    Long paymentId,

    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey
) {
}
