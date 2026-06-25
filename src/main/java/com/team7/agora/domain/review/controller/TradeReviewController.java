package com.team7.agora.domain.review.controller;

import com.team7.agora.domain.review.dto.response.ReviewResponse;
import com.team7.agora.domain.review.service.ReviewService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TradeReviewController {

    private final ReviewService reviewService;

    public TradeReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/trades/{tradeId}/reviews")
    public ApiResponse<List<ReviewResponse>> getTradeReviews(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        List<ReviewResponse> response = reviewService.getTradeReviews(authUser.userId(), tradeId);
        return ApiResponse.success("거래 후기 조회가 완료되었습니다.", response);
    }
}
