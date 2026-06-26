package com.team7.agora.domain.review.controller;

import com.team7.agora.domain.review.dto.request.ReviewCreateRequest;
import com.team7.agora.domain.review.dto.response.ReviewResponse;
import com.team7.agora.domain.review.service.ReviewService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 리뷰 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param reviewService 리뷰 비즈니스 로직을 처리하는 서비스
     */
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 리뷰 정보를 생성하거나 준비하는 POST /api/reviews 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping
    public ApiResponse<ReviewResponse> create(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewResponse response = reviewService.create(
            userDetails.getUserId(),
            request.tradeId(),
            request.rating(),
            request.content()
        );
        return ApiResponse.success("후기가 등록되었습니다.", response);
    }
}
