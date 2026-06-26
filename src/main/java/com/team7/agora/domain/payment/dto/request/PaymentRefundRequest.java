package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payment Refund 요청 본문을 표현하는 DTO이다.
 * @param reason 처리 사유
 */
public record PaymentRefundRequest(
    @NotBlank(message = "환불 사유는 필수입니다.")
    @Size(max = 500, message = "환불 사유는 500자 이하로 입력해 주세요.")
    String reason
) {
}
