package com.team7.agora.domain.trade.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying trade data.
 */
public interface TradeRepository extends JpaRepository<Trade, Long> {

    boolean existsByProduct(Product product);

    boolean existsByProductAndStatusNot(Product product, TradeStatus status);

    Optional<Trade> findByProduct(Product product);

    List<Trade> findAllByStatusAndPaymentDueAtLessThanEqual(TradeStatus status, LocalDateTime now);
}
