package com.team7.agora.domain.trade.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 거래 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface TradeRepository extends JpaRepository<Trade, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trade t where t.id = :id")
    Optional<Trade> findByIdForUpdate(@Param("id") Long id);

    boolean existsByProduct(Product product);

    boolean existsByProductAndStatusNot(Product product, TradeStatus status);

    Optional<Trade> findByProduct(Product product);

    List<Trade> findAllByStatusAndPaymentDueAtLessThanEqual(TradeStatus status, LocalDateTime now);
}
