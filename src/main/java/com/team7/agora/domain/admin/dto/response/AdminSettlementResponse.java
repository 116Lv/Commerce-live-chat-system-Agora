package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.settlement.entity.Settlement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminSettlementResponse(
    Long settlementId,
    Long paymentId,
    Long sellerId,
    BigDecimal amount,
    String status,
    LocalDateTime settledAt
) {

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
