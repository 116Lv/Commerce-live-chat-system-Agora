// 결제 승인 API 요청 DTO입니다.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for payment confirm by id operations.
 * @param paymentId the payment id value
 * @param paymentKey the payment key value
 */
public record PaymentConfirmByIdRequest(
    @NotNull(message = "결제 ID는 필수입니다.")
    Long paymentId,

    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey
) {
}
