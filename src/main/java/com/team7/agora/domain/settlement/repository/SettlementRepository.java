package com.team7.agora.domain.settlement.repository;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.entity.Settlement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying settlement data.
 */
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    boolean existsByPayment(Payment payment);

    Optional<Settlement> findByPaymentTradeId(Long tradeId);
}
