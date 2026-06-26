package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for payment webhook operations.
 * @param orderId the order id value
 * @param paymentKey the payment key value
 * @param status the status value
 */
public record PaymentWebhookRequest(
    @NotBlank(message = "orderId는 필수입니다.")
    String orderId,

    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey,

    @NotBlank(message = "status는 필수입니다.")
    String status
) {
}
