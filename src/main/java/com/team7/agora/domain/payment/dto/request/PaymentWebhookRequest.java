package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentWebhookRequest(
    @NotBlank(message = "orderId는 필수입니다.")
    String orderId,

    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey,

    @NotBlank(message = "status는 필수입니다.")
    String status
) {
}
