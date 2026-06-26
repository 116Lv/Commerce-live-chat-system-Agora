package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for payment refund operations.
 * @param reason the reason value
 */
public record PaymentRefundRequest(
    @NotBlank(message = "환불 사유는 필수입니다.")
    @Size(max = 500, message = "환불 사유는 500자 이하로 입력해 주세요.")
    String reason
) {
}
