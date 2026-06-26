// 결제 승인 API 요청 DTO입니다.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param paymentId 입력 값
 * @param paymentKey 입력 값
 */
public record PaymentConfirmByIdRequest(
    @NotNull(message = "결제 ID는 필수입니다.")
    Long paymentId,

    @NotBlank(message = "paymentKey는 필수입니다.")
    String paymentKey
) {
}
