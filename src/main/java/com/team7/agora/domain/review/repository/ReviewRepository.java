package com.team7.agora.domain.review.repository;

import com.team7.agora.domain.review.entity.Review;
import com.team7.agora.domain.trade.entity.Trade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying review data.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByTradeAndReviewerId(Trade trade, Long reviewerId);

    List<Review> findAllByTrade(Trade trade);
}
