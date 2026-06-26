// 결제 환불 상태 조회 응답 DTO
package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param paymentId 입력 값
 * @param tradeId 입력 값
 * @param status 입력 값
 * @param refundedAt 입력 값
 */
public record RefundStatusResponse(
    Long paymentId,
    Long tradeId,
    String status,
    LocalDateTime refundedAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param payment 입력 값
     * @return 처리 결과
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
