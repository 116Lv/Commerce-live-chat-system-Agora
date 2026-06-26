package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param paymentId 입력 값
 * @param amount 입력 값
 * @param orderId 입력 값
 * @param paymentKey 입력 값
 * @param status 입력 값
 * @param requestedAt 입력 값
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
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param payment 입력 값
     * @return 처리 결과
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
