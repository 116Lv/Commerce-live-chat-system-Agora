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
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TradeRepository tradeRepository;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param reviewRepository 입력 값
     * @param tradeRepository 입력 값
     */
    public ReviewService(ReviewRepository reviewRepository, TradeRepository tradeRepository) {
        this.reviewRepository = reviewRepository;
        this.tradeRepository = tradeRepository;
    }

    /**
     * 도메인 객체를 생성한다.
     * @param reviewerId 입력 값
     * @param tradeId 입력 값
     * @param rating 입력 값
     * @param content 입력 값
     * @return 처리 결과
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
     * 데이터를 반환한다.
     * @param userId 입력 값
     * @param tradeId 입력 값
     * @return 처리 결과
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
