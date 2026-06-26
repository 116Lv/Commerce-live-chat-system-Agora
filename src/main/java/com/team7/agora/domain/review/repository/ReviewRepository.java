package com.team7.agora.domain.review.repository;

import com.team7.agora.domain.review.entity.Review;
import com.team7.agora.domain.trade.entity.Trade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 리뷰 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByTradeAndReviewerId(Trade trade, Long reviewerId);

    List<Review> findAllByTrade(Trade trade);
}
