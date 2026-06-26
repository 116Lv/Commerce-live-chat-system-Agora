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

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/users/me/coupons")
public class MyCouponController {

    private final CouponQueryService couponQueryService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param couponQueryService 입력 값
     */
    public MyCouponController(CouponQueryService couponQueryService) {
        this.couponQueryService = couponQueryService;
    }

    /**
     * 데이터를 반환한다.
     * @param userDetails 입력 값
     * @return 처리 결과
     */
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping
    public ApiResponse<List<MyCouponResponse>> getMyCoupons(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MyCouponResponse> response = couponQueryService.getMyCoupons(userDetails.getUserId());
        return ApiResponse.success("내 쿠폰 목록 조회가 완료되었습니다.", response);
    }
}
