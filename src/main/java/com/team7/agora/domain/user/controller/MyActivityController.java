package com.team7.agora.domain.user.controller;

import com.team7.agora.domain.review.dto.request.MyReviewType;
import com.team7.agora.domain.review.dto.response.MyReviewResponse;
import com.team7.agora.domain.review.service.ReviewService;
import com.team7.agora.domain.trade.dto.request.MyTradeRole;
import com.team7.agora.domain.trade.dto.response.MyTradeResponse;
import com.team7.agora.domain.trade.service.TradeService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.response.ApiResponse;
import com.team7.agora.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class MyActivityController {

    private final TradeService tradeService;
    private final ReviewService reviewService;

    public MyActivityController(TradeService tradeService, ReviewService reviewService) {
        this.tradeService = tradeService;
        this.reviewService = reviewService;
    }

    @GetMapping("/trades")
    public ApiResponse<PageResponse<MyTradeResponse>> getMyTrades(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String role,
        @RequestParam(defaultValue = "0") String page,
        @RequestParam(defaultValue = "20") String size
    ) {
        int pageNumber = parsePage(page);
        int pageSize = parseSize(size);
        Page<MyTradeResponse> responses = tradeService.getMyTrades(
            userDetails.getUserId(),
            MyTradeRole.from(role),
            PageRequest.of(pageNumber, pageSize)
        );
        return ApiResponse.success("거래 목록을 조회했습니다.", PageResponse.from(responses));
    }

    @GetMapping("/reviews")
    public ApiResponse<PageResponse<MyReviewResponse>> getMyReviews(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "0") String page,
        @RequestParam(defaultValue = "20") String size
    ) {
        int pageNumber = parsePage(page);
        int pageSize = parseSize(size);
        Page<MyReviewResponse> responses = reviewService.getMyReviews(
            userDetails.getUserId(),
            MyReviewType.from(type),
            PageRequest.of(pageNumber, pageSize)
        );
        return ApiResponse.success("Review list retrieved.", PageResponse.from(responses));
    }

    private int parsePage(String value) {
        int page = parseInteger(value, "page must be a number.");
        if (page < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "page must be greater than or equal to 0.");
        }
        return page;
    }

    private int parseSize(String value) {
        int size = parseInteger(value, "size must be a number.");
        if (size < 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "size must be greater than 0.");
        }
        return size;
    }

    private int parseInteger(String value, String message) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }
    }
}
