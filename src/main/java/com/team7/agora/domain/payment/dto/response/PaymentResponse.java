package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.math.BigDecimal;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param paymentId 입력 값
 * @param tradeId 입력 값
 * @param payerId 입력 값
 * @param amount 입력 값
 * @param orderId 입력 값
 * @param paymentKey 입력 값
 * @param status 입력 값
 * @param settlementId 입력 값
 */
public record PaymentResponse(
    Long paymentId,
    Long tradeId,
    Long payerId,
    BigDecimal amount,
    String orderId,
    String paymentKey,
    String status,
    Long settlementId
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param payment 입력 값
     * @return 처리 결과
     */
    public static PaymentResponse from(Payment payment) {
        return from(payment, null);
    }

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param payment 입력 값
     * @param settlementId 입력 값
     * @return 처리 결과
     */
    public static PaymentResponse from(Payment payment, Long settlementId) {
        return new PaymentResponse(
            payment.getId(),
            payment.getTrade().getId(),
            payment.getPayer().getId(),
            payment.getAmount(),
            payment.getOrderId(),
            payment.getPaymentKey(),
            payment.getStatus().name(),
            settlementId
        );
    }
}
