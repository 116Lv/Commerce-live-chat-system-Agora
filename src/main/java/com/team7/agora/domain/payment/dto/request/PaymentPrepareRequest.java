// 결제 준비 API 요청 DTO입니다.
package com.team7.agora.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param tradeId 입력 값
 */
public record PaymentPrepareRequest(
    @NotNull(message = "거래 ID는 필수입니다.")
    Long tradeId
) {
}
