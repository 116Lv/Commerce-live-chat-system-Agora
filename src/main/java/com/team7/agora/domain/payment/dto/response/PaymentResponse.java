package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.math.BigDecimal;

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

    public static PaymentResponse from(Payment payment) {
        return from(payment, null);
    }

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
