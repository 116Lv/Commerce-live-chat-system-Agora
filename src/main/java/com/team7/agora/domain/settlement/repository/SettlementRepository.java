package com.team7.agora.domain.settlement.repository;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.entity.Settlement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    boolean existsByPayment(Payment payment);

    Optional<Settlement> findByPaymentTradeId(Long tradeId);
}
