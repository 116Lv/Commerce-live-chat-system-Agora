package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload for returning admin payment data.
 * @param paymentId the payment id value
 * @param amount the amount value
 * @param orderId the order id value
 * @param paymentKey the payment key value
 * @param status the status value
 * @param requestedAt the requested at value
 */
public record AdminPaymentResponse(
    Long paymentId,
    BigDecimal amount,
    String orderId,
    String paymentKey,
    String status,
    LocalDateTime requestedAt
) {

    /**
     * Creates a response from the given domain object.
     * @param payment the payment value
     * @return the from result
     */
    public static AdminPaymentResponse from(Payment payment) {
        return new AdminPaymentResponse(
            payment.getId(),
            payment.getAmount(),
            payment.getOrderId(),
            payment.getPaymentKey(),
            payment.getStatus().name(),
            payment.getRequestedAt()
        );
    }
}
