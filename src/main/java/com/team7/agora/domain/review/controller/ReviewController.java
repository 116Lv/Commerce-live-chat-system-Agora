package com.team7.agora.domain.review.controller;

import com.team7.agora.domain.review.dto.request.ReviewCreateRequest;
import com.team7.agora.domain.review.dto.response.ReviewResponse;
import com.team7.agora.domain.review.service.ReviewService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes review endpoints.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Creates a review controller instance.
     * @param reviewService the review service value
     */
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Creates create data.
     * @param authUser the auth user value
     * @param request the request value
     * @return the create result
     */
    @PostMapping
    public ApiResponse<ReviewResponse> create(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewResponse response = reviewService.create(
            authUser.userId(),
            request.tradeId(),
            request.rating(),
            request.content()
        );
        return ApiResponse.success("후기가 등록되었습니다.", response);
    }
}
