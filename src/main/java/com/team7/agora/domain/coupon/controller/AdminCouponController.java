package com.team7.agora.domain.coupon.controller;

import com.team7.agora.domain.coupon.dto.request.AdminCouponCreateRequest;
import com.team7.agora.domain.coupon.dto.request.AdminCouponIssueRequest;
import com.team7.agora.domain.coupon.dto.response.AdminCouponResponse;
import com.team7.agora.domain.coupon.dto.response.CouponBroadcastResponse;
import com.team7.agora.domain.coupon.dto.response.CouponIssueHistoryResponse;
import com.team7.agora.domain.coupon.service.AdminCouponService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    public AdminCouponController(AdminCouponService adminCouponService) {
        this.adminCouponService = adminCouponService;
    }

    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @PostMapping
    public ApiResponse<AdminCouponResponse> create(
        @AuthenticationPrincipal CustomUserDetails admin,
        @Valid @RequestBody AdminCouponCreateRequest request
    ) {
        AdminCouponResponse response = adminCouponService.create(
            admin,
            request.name(),
            request.discountAmount(),
            request.minOrderAmount(),
            request.type(),
            request.validDays()
        );
        return ApiResponse.success("쿠폰 정책이 생성되었습니다.", response);
    }
    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @GetMapping("/{couponId}")
    public ApiResponse<AdminCouponResponse> getDetail(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long couponId
    ) {
        AdminCouponResponse response = adminCouponService.getDetail(admin, couponId);
        return ApiResponse.success("쿠폰 정책 상세를 조회했습니다.", response);
    }
    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @PostMapping("/{couponId}/issue")
    public ApiResponse<CouponBroadcastResponse> issue(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long couponId,
        @Valid @RequestBody AdminCouponIssueRequest request
    ) {
        CouponBroadcastResponse response = adminCouponService.issueToUsers(admin, couponId, request.userIds());
        return ApiResponse.success("지정 사용자에게 쿠폰을 발급했습니다.", response);
    }
    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @GetMapping("/{couponId}/issues")
    public ApiResponse<CouponIssueHistoryResponse> getIssueHistory(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long couponId
    ) {
        CouponIssueHistoryResponse response = adminCouponService.getIssueHistory(admin, couponId);
        return ApiResponse.success("쿠폰 발급 내역을 조회했습니다.", response);
    }
}
