// 결제 환불 상태 조회 응답 DTO
package com.team7.agora.domain.payment.dto.response;

import com.team7.agora.domain.payment.entity.Payment;
import java.time.LocalDateTime;

/**
 * Refund Status 응답 본문을 표현하는 DTO이다.
 * @param paymentId 결제 ID
 * @param tradeId 거래 ID
 * @param status 조회 또는 변경할 상태
 * @param refundedAt 환불이 완료된 시각
 */
public record RefundStatusResponse(
    Long paymentId,
    Long tradeId,
    String status,
    LocalDateTime refundedAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param payment 결제 엔티티 또는 결제 응답 변환 대상
     * @return 클라이언트에 반환할 API 응답
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
