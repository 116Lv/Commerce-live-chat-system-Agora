package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.math.BigDecimal;

/**
 * Response payload for returning payment data.
 * @param paymentId the payment id value
 * @param tradeId the trade id value
 * @param payerId the payer id value
 * @param amount the amount value
 * @param orderId the order id value
 * @param paymentKey the payment key value
 * @param status the status value
 * @param settlementId the settlement id value
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
     * Creates a response from the given domain object.
     * @param payment the payment value
     * @return the from result
     */
    public static PaymentResponse from(Payment payment) {
        return from(payment, null);
    }

    /**
     * Creates a response from the given domain object.
     * @param payment the payment value
     * @param settlementId the settlement id value
     * @return the from result
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
