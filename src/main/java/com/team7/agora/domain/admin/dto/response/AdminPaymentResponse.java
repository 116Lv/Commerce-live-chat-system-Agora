package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentResponse(
    Long paymentId,
    BigDecimal amount,
    String orderId,
    String paymentKey,
    String status,
    LocalDateTime requestedAt
) {

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
