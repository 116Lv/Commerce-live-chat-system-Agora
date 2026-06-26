package com.team7.agora.domain.review.repository;

import com.team7.agora.domain.review.entity.Review;
import com.team7.agora.domain.trade.entity.Trade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByTradeAndReviewerId(Trade trade, Long reviewerId);

    List<Review> findAllByTrade(Trade trade);
}
