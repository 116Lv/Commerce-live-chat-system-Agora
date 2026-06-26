package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.settlement.entity.Settlement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param settlementId 입력 값
 * @param paymentId 입력 값
 * @param sellerId 입력 값
 * @param amount 입력 값
 * @param status 입력 값
 * @param settledAt 입력 값
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
     * @param settlement 입력 값
     * @return 처리 결과
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
