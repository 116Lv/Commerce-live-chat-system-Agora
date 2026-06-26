package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.settlement.entity.Settlement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 관리자 정산 응답 본문을 표현하는 DTO이다.
 * @param settlementId 정산 ID
 * @param paymentId 결제 ID
 * @param sellerId 상품 판매자 ID
 * @param amount 금액
 * @param status 조회 또는 변경할 상태
 * @param settledAt 정산이 완료된 시각
 */
public record AdminSettlementResponse(
    Long settlementId,
    Long paymentId,
    Long sellerId,
    BigDecimal amount,
    String status,
    LocalDateTime settledAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param settlement 정산 엔티티 또는 정산 응답 변환 대상
     * @return 클라이언트에 반환할 API 응답
     */
    public static AdminSettlementResponse from(Settlement settlement) {
        return new AdminSettlementResponse(
            settlement.getId(),
            settlement.getPayment().getId(),
            settlement.getSeller().getId(),
            settlement.getAmount(),
            settlement.getStatus().name(),
            settlement.getSettledAt()
        );
    }
}
