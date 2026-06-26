package com.team7.agora.domain.trade.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 거래 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface TradeRepository extends JpaRepository<Trade, Long> {

    boolean existsByProduct(Product product);

    boolean existsByProductAndStatusNot(Product product, TradeStatus status);

    Optional<Trade> findByProduct(Product product);

    List<Trade> findAllByStatusAndPaymentDueAtLessThanEqual(TradeStatus status, LocalDateTime now);
}
