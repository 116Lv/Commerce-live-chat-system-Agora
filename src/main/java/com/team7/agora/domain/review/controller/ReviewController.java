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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param reviewService 입력 값
     */
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 도메인 객체를 생성한다.
     * @param userDetails 입력 값
     * @param request 입력 값
     * @return 처리 결과
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
