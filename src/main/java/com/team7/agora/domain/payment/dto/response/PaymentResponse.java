package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.math.BigDecimal;

/**
 * 결제 응답 본문을 표현하는 DTO이다.
 * @param paymentId 결제 ID
 * @param tradeId 거래 ID
 * @param payerId 결제자 ID
 * @param amount 금액
 * @param orderId 주문 ID
 * @param paymentKey 결제 승인 키
 * @param status 조회 또는 변경할 상태
 * @param settlementId 정산 ID
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
     * @param payment 결제 엔티티 또는 결제 응답 변환 대상
     * @return 클라이언트에 반환할 API 응답
     */
    public static PaymentResponse from(Payment payment) {
        return from(payment, null);
    }

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param payment 결제 엔티티 또는 결제 응답 변환 대상
     * @param settlementId 정산 ID
     * @return 클라이언트에 반환할 API 응답
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
