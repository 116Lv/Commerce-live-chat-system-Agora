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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
public class TradeReviewController {

    private final ReviewService reviewService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param reviewService 입력 값
     */
    public TradeReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 데이터를 반환한다.
     * @param userDetails 입력 값
     * @param tradeId 입력 값
     * @return 처리 결과
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
