package com.team7.agora.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.review.dto.request.MyReviewType;
import com.team7.agora.domain.review.dto.response.MyReviewResponse;
import com.team7.agora.domain.review.entity.Review;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReviewQueryRepositoryImplTest {

    @Autowired
    private ReviewQueryRepository reviewQueryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser;
    private User seller;
    private User buyer;
    private User unrelatedReviewer;
    private User unrelatedTarget;
    private Region region;

    @BeforeEach
    void setUp() {
        currentUser = userRepository.save(User.signup("my-reviews-current@test.com", "encoded", "current", "01010000001"));
        seller = userRepository.save(User.signup("my-reviews-seller@test.com", "encoded", "seller", "01010000002"));
        buyer = userRepository.save(User.signup("my-reviews-buyer@test.com", "encoded", "buyer", "01010000003"));
        unrelatedReviewer = userRepository.save(User.signup("my-reviews-unrelated-reviewer@test.com", "encoded", "unrelatedReviewer", "01010000004"));
        unrelatedTarget = userRepository.save(User.signup("my-reviews-unrelated-target@test.com", "encoded", "unrelatedTarget", "01010000005"));
        region = regionRepository.save(Region.create("my reviews region", "MYREVIEWS001", "sido", "sigungu", "dong"));
    }

    @Test
    void findMyReviews_writtenReturnsOnlyReviewsByCurrentUser() {
        Review written = reviewRepository.save(createReview(currentUser, buyer, "written product", "written content", 5));
        reviewRepository.save(createReview(seller, currentUser, "received product", "received content", 4));
        reviewRepository.save(createReview(unrelatedReviewer, unrelatedTarget, "unrelated product", "unrelated content", 3));

        Page<MyReviewResponse> responses = reviewQueryRepository.findMyReviews(
            currentUser.getId(),
            MyReviewType.WRITTEN,
            PageRequest.of(0, 20)
        );

        assertThat(responses.getTotalElements()).isEqualTo(1);
        MyReviewResponse response = responses.getContent().get(0);
        assertThat(response.reviewId()).isEqualTo(written.getId());
        assertThat(response.productTitle()).isEqualTo("written product");
        assertThat(response.reviewerNickname()).isEqualTo("current");
        assertThat(response.targetNickname()).isEqualTo("buyer");
        assertThat(response.content()).isEqualTo("written content");
    }

    @Test
    void findMyReviews_receivedReturnsOnlyReviewsTargetingCurrentUser() {
        reviewRepository.save(createReview(currentUser, buyer, "written product", "written content", 5));
        Review received = reviewRepository.save(createReview(seller, currentUser, "received product", "received content", 4));
        reviewRepository.save(createReview(unrelatedReviewer, unrelatedTarget, "unrelated product", "unrelated content", 3));

        Page<MyReviewResponse> responses = reviewQueryRepository.findMyReviews(
            currentUser.getId(),
            MyReviewType.RECEIVED,
            PageRequest.of(0, 20)
        );

        assertThat(responses.getTotalElements()).isEqualTo(1);
        MyReviewResponse response = responses.getContent().get(0);
        assertThat(response.reviewId()).isEqualTo(received.getId());
        assertThat(response.productTitle()).isEqualTo("received product");
        assertThat(response.reviewerNickname()).isEqualTo("seller");
        assertThat(response.targetNickname()).isEqualTo("current");
        assertThat(response.rating()).isEqualTo(4);
    }

    @Test
    void findMyReviews_ordersByCreatedAtDescThenIdDesc() {
        Review newer = reviewRepository.save(createReview(currentUser, buyer, "newer product", "newer content", 5));
        Review older = reviewRepository.save(createReview(currentUser, buyer, "older product", "older content", 4));
        ReflectionTestUtils.setField(newer, "createdAt", LocalDateTime.of(2026, 1, 2, 0, 0));
        ReflectionTestUtils.setField(older, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));

        Page<MyReviewResponse> responses = reviewQueryRepository.findMyReviews(
            currentUser.getId(),
            MyReviewType.WRITTEN,
            PageRequest.of(0, 20)
        );

        assertThat(responses).extracting(MyReviewResponse::reviewId)
            .containsExactly(newer.getId(), older.getId());
    }

    private Review createReview(User reviewer, User targetUser, String productTitle, String content, int rating) {
        Product product = productRepository.save(Product.create(
            reviewer,
            region,
            productTitle,
            "description",
            BigDecimal.valueOf(10000),
            "MY_REVIEWS_TEST"
        ));
        Trade trade = tradeRepository.save(Trade.start(product, reviewer, targetUser, BigDecimal.valueOf(9000)));
        return Review.create(trade, reviewer, targetUser, rating, content);
    }
}
