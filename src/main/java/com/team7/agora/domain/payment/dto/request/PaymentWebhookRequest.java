package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 결제 웹훅 요청 본문을 표현하는 DTO이다.
 * @param orderId 주문 ID
 * @param paymentKey 결제 승인 키
 * @param status 조회 또는 변경할 상태
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
