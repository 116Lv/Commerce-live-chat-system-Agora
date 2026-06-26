package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param paymentKey 입력 값
 */
public record PaymentConfirmRequest(
    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey
) {
}
