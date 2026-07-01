package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.user.entity.User;
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
 * @param buyerEmail 구매자 이메일
 * @param buyerName 구매자 이름
 * @param buyerTel 구매자 연락처
 */
public record PaymentResponse(
    Long paymentId,
    Long tradeId,
    Long payerId,
    BigDecimal amount,
    String orderId,
    String paymentKey,
    String status,
    Long settlementId,
    String buyerEmail,
    String buyerName,
    String buyerTel
) {
    public PaymentResponse(
        Long paymentId,
        Long tradeId,
        Long payerId,
        BigDecimal amount,
        String orderId,
        String paymentKey,
        String status,
        Long settlementId
    ) {
        this(paymentId, tradeId, payerId, amount, orderId, paymentKey, status, settlementId, null, null, null);
    }

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
        User payer = payment.getPayer();
        return new PaymentResponse(
            payment.getId(),
            payment.getTrade().getId(),
            payment.getPayer().getId(),
            payment.getAmount(),
            payment.getOrderId(),
            payment.getPaymentKey(),
            payment.getStatus().name(),
            settlementId,
            payer.getEmail(),
            payer.getNickname(),
            payer.getPhone()
        );
    }
}
