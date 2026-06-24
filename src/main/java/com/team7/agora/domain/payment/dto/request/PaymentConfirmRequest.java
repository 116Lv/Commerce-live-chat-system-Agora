package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentConfirmRequest(
    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey
) {
}
