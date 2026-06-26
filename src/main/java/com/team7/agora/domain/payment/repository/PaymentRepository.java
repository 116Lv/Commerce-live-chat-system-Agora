package com.team7.agora.domain.payment.repository;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.trade.entity.Trade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying payment data.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByTrade(Trade trade);

    Optional<Payment> findByTrade(Trade trade);

    Optional<Payment> findByOrderId(String orderId);

    Page<Payment> findAllByStatus(PaymentStatus status, Pageable pageable);

    List<Payment> findAllByStatus(PaymentStatus status);
}
