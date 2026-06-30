package com.team7.agora.domain.review.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private ReviewQueryRepository reviewQueryRepository;

    private ReviewService reviewService;
    private User seller;
    private User buyer;
    private User stranger;
    private Trade trade;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, reviewQueryRepository, tradeRepository);
        seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        stranger = User.signup("stranger@test.com", "password", "제3자", "01055556666");
        assignId(stranger, 3L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        Product product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
        trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(50000));
        assignId(trade, 100L);
    }

    @Test
    void createReviewUpdatesTargetSmileScore() {
        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));
        when(reviewRepository.existsByTradeAndReviewerId(trade, 2L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            assignId(review, 1000L);
            return review;
        });

        ReviewResponse response = reviewService.create(2L, 100L, 5, "친절하고 빠른 거래였어요.");

        assertThat(response.reviewId()).isEqualTo(1000L);
        assertThat(response.reviewerId()).isEqualTo(2L);
        assertThat(response.targetUserId()).isEqualTo(1L);
        assertThat(seller.getSmileScore()).isGreaterThan(60);
    }

    @Test
    void createReviewRejectsNonParticipant() {
        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));

        assertThatThrownBy(() -> reviewService.create(3L, 100L, 5, "좋아요"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void createReviewRejectsDuplicateReviewer() {
        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));
        when(reviewRepository.existsByTradeAndReviewerId(trade, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(2L, 100L, 5, "좋아요"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void createReviewRejectsSellerReviewingOwnSoldProduct() {
        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));

        assertThatThrownBy(() -> reviewService.create(1L, 100L, 5, "seller review"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void getTradeReviewsReturnsReviewsForParticipant() {
        Review review = Review.create(trade, buyer, seller, 5, "친절해요");
        assignId(review, 1000L);

        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));
        when(reviewRepository.findAllByTrade(trade)).thenReturn(java.util.List.of(review));

        var responses = reviewService.getTradeReviews(1L, 100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).reviewId()).isEqualTo(1000L);
    }

    @Test
    void getTradeReviewsRejectsNonParticipant() {
        when(tradeRepository.findById(100L)).thenReturn(Optional.of(trade));

        assertThatThrownBy(() -> reviewService.getTradeReviews(3L, 100L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void getMyReviewsDelegatesToQueryRepository() {
        PageRequest pageable = PageRequest.of(0, 20);
        MyReviewResponse expected = new MyReviewResponse(
            1000L,
            100L,
            10L,
            "bike",
            "buyer",
            "seller",
            5,
            "kind trade",
            LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        when(reviewQueryRepository.findMyReviews(2L, MyReviewType.WRITTEN, pageable))
            .thenReturn(new PageImpl<>(java.util.List.of(expected), pageable, 1));

        var responses = reviewService.getMyReviews(2L, MyReviewType.WRITTEN, pageable);

        assertThat(responses.getTotalElements()).isEqualTo(1);
        assertThat(responses.getContent()).containsExactly(expected);
    }
}
