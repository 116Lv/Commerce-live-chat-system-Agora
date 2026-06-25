// 결제 환불 상태 조회 응답 DTO
package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.time.LocalDateTime;

public record RefundStatusResponse(
    Long paymentId,
    Long tradeId,
    String status,
    LocalDateTime refundedAt
) {

    public static RefundStatusResponse from(Payment payment) {
        return new RefundStatusResponse(
            payment.getId(),
            payment.getTrade().getId(),
            payment.getStatus().name(),
            payment.getRefundedAt()
        );
    }
}
