package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Payment Confirm 요청 본문을 표현하는 DTO이다.
 * @param paymentKey 결제 승인 키
 */
public record PaymentConfirmRequest(
    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey
) {
}
