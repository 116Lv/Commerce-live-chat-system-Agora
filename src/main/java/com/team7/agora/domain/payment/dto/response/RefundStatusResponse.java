// 결제 환불 상태 조회 응답 DTO
package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.time.LocalDateTime;

/**
 * Response payload for returning refund status data.
 * @param paymentId the payment id value
 * @param tradeId the trade id value
 * @param status the status value
 * @param refundedAt the refunded at value
 */
public record RefundStatusResponse(
    Long paymentId,
    Long tradeId,
    String status,
    LocalDateTime refundedAt
) {

    /**
     * Creates a response from the given domain object.
     * @param payment the payment value
     * @return the from result
     */
    public static RefundStatusResponse from(Payment payment) {
        return new RefundStatusResponse(
            payment.getId(),
            payment.getTrade().getId(),
            payment.getStatus().name(),
            payment.getRefundedAt()
        );
    }
}
