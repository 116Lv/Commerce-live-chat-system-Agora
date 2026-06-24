package com.team7.agora.domain.coupon.controller;

import com.team7.agora.domain.coupon.dto.response.CouponEventResponse;
import com.team7.agora.domain.coupon.service.CouponIssueService;
import com.team7.agora.domain.coupon.service.CouponQueryService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupon-events")
public class CouponEventController {

    private final CouponIssueService couponIssueService;
    private final CouponQueryService couponQueryService;

    public CouponEventController(CouponIssueService couponIssueService, CouponQueryService couponQueryService) {
        this.couponIssueService = couponIssueService;
        this.couponQueryService = couponQueryService;
    }

    @GetMapping
    public ApiResponse<List<CouponEventResponse>> list() {
        return ApiResponse.success("진행중인 쿠폰 이벤트 목록입니다.", couponQueryService.listActiveEvents());
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/{eventId}/issue")
    public ResponseEntity<ApiResponse<Void>> issue(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long eventId
    ) {
        couponIssueService.issue(userDetails.getUserId(), eventId);
        return ResponseEntity.ok(ApiResponse.success("쿠폰이 발급되었습니다.", null));
    }
}
