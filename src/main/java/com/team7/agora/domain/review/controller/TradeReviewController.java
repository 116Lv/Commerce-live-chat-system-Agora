package com.team7.agora.domain.review.controller;

import com.team7.agora.domain.review.dto.response.ReviewResponse;
import com.team7.agora.domain.review.service.ReviewService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes trade review endpoints.
 */
@RestController
public class TradeReviewController {

    private final ReviewService reviewService;

    /**
     * Creates a trade review controller instance.
     * @param reviewService the review service value
     */
    public TradeReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Returns trade reviews data.
     * @param userDetails the auth user value
     * @param tradeId the trade id value
     * @return the get trade reviews result
     */
    @GetMapping("/api/trades/{tradeId}/reviews")
    public ApiResponse<List<ReviewResponse>> getTradeReviews(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long tradeId
    ) {
        List<ReviewResponse> response = reviewService.getTradeReviews(userDetails.getUserId(), tradeId);
        return ApiResponse.success("거래 후기 조회가 완료되었습니다.", response);
    }
}
