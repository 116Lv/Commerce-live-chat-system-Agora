// 결제 승인 API 요청 DTO입니다.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payment Confirm By Id 요청 본문을 표현하는 DTO이다.
 * @param paymentId 결제 ID
 * @param paymentKey 결제 승인 키
 */
public record PaymentConfirmByIdRequest(
    @NotNull(message = "결제 ID는 필수입니다.")
    Long paymentId,

    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey
) {
}
