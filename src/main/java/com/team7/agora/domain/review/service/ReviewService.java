package com.team7.agora.domain.review.service;

import com.team7.agora.domain.review.dto.request.MyReviewType;
import com.team7.agora.domain.review.dto.response.MyReviewResponse;
import com.team7.agora.domain.review.dto.response.ReviewResponse;
import com.team7.agora.domain.review.entity.Review;
import com.team7.agora.domain.review.repository.ReviewQueryRepository;
import com.team7.agora.domain.review.repository.ReviewRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 리뷰 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final TradeRepository tradeRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param reviewRepository 데이터를 조회하고 저장하는 리포지토리
     * @param tradeRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public ReviewService(
        ReviewRepository reviewRepository,
        ReviewQueryRepository reviewQueryRepository,
        TradeRepository tradeRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewQueryRepository = reviewQueryRepository;
        this.tradeRepository = tradeRepository;
    }

    public Page<MyReviewResponse> getMyReviews(Long userId, MyReviewType type, Pageable pageable) {
        return reviewQueryRepository.findMyReviews(userId, type, pageable);
    }

    /**
     * 거래 완료 여부를 검증한 뒤 상대방에게 남길 후기를 생성한다.
     * @param reviewerId 후기를 작성한 회원 ID
     * @param tradeId 거래 ID
     * @param rating 후기 평점
     * @param content 내용
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ReviewResponse create(Long reviewerId, Long tradeId, int rating, String content) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        if (!trade.isParticipant(reviewerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "거래 참여자만 후기를 작성할 수 있습니다.");
        }
        if (trade.getSeller().getId().equals(reviewerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "구매자만 거래 후기를 작성할 수 있습니다.");
        }
        if (reviewRepository.existsByTradeAndReviewerId(trade, reviewerId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 후기를 작성한 거래입니다.");
        }

        User reviewer = trade.getBuyer();
        User targetUser = trade.getCounterpart(reviewerId);
        Review review = Review.create(trade, reviewer, targetUser, rating, content);
        targetUser.updateSmileScore(toSmileDelta(rating));
        return ReviewResponse.from(reviewRepository.save(review));
    }

    private int toSmileDelta(int rating) {
        return switch (rating) {
            case 1 -> -4;
            case 2 -> -2;
            case 3 -> 0;
            case 4 -> 2;
            case 5 -> 4;
            default -> throw new IllegalArgumentException("평점은 1점부터 5점까지 입력할 수 있습니다.");
        };
    }

    /**
     * 'getTradeReviews' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param userId 회원 ID
     * @param tradeId 거래 ID
     * @return 클라이언트에 반환할 API 응답
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
