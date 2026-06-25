package com.team7.agora.domain.coupon.controller;

import com.team7.agora.domain.coupon.dto.response.MyCouponResponse;
import com.team7.agora.domain.coupon.service.CouponQueryService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/coupons")
public class MyCouponController {

    private final CouponQueryService couponQueryService;

    public MyCouponController(CouponQueryService couponQueryService) {
        this.couponQueryService = couponQueryService;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping
    public ApiResponse<List<MyCouponResponse>> getMyCoupons(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MyCouponResponse> response = couponQueryService.getMyCoupons(userDetails.getUserId());
        return ApiResponse.success("내 쿠폰 목록 조회가 완료되었습니다.", response);
    }
}
