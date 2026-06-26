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
 * 거래 리뷰 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
public class TradeReviewController {

    private final ReviewService reviewService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param reviewService 리뷰 비즈니스 로직을 처리하는 서비스
     */
    public TradeReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 거래 리뷰 정보를 조회하는 GET /api/trades/{tradeId}/reviews 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param tradeId 대상 거래 ID
     * @return 클라이언트에 반환할 API 응답
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
