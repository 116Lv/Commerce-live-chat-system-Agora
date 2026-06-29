package com.team7.agora.domain.settlement.repository;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.entity.Settlement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Settlement 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    boolean existsByPayment(Payment payment);

    Optional<Settlement> findByPayment(Payment payment);

    Optional<Settlement> findByPaymentTradeId(Long tradeId);
}
