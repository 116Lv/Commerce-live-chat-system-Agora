package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.settlement.entity.Settlement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload for returning admin settlement data.
 * @param settlementId the settlement id value
 * @param paymentId the payment id value
 * @param sellerId the seller id value
 * @param amount the amount value
 * @param status the status value
 * @param settledAt the settled at value
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
     * Creates a response from the given domain object.
     * @param settlement the settlement value
     * @return the from result
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
