package com.team7.agora.domain.review.service;

import com.team7.agora.domain.review.dto.response.ReviewResponse;
import com.team7.agora.domain.review.entity.Review;
import com.team7.agora.domain.review.repository.ReviewRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that coordinates review use cases.
 */
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TradeRepository tradeRepository;

    /**
     * Creates a review service instance.
     * @param reviewRepository the review repository value
     * @param tradeRepository the trade repository value
     */
    public ReviewService(ReviewRepository reviewRepository, TradeRepository tradeRepository) {
        this.reviewRepository = reviewRepository;
        this.tradeRepository = tradeRepository;
    }

    /**
     * Creates create data.
     * @param reviewerId the reviewer id value
     * @param tradeId the trade id value
     * @param rating the rating value
     * @param content the content value
     * @return the create result
     */
    @Transactional
    public ReviewResponse create(Long reviewerId, Long tradeId, int rating, String content) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        if (!trade.isParticipant(reviewerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "거래 참여자만 후기를 작성할 수 있습니다.");
        }
        if (reviewRepository.existsByTradeAndReviewerId(trade, reviewerId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 후기를 작성한 거래입니다.");
        }

        User reviewer = reviewerId.equals(trade.getSeller().getId()) ? trade.getSeller() : trade.getBuyer();
        User targetUser = trade.getCounterpart(reviewerId);
        Review review = Review.create(trade, reviewer, targetUser, rating, content);
        return ReviewResponse.from(reviewRepository.save(review));
    }

    /**
     * Returns trade reviews data.
     * @param userId the user id value
     * @param tradeId the trade id value
     * @return the get trade reviews result
     */
    public List<ReviewResponse> getTradeReviews(Long userId, Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        if (!trade.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "거래 참여자만 후기를 조회할 수 있습니다.");
        }

        return reviewRepository.findAllByTrade(trade).stream()
            .map(ReviewResponse::from)
            .toList();
    }
}
