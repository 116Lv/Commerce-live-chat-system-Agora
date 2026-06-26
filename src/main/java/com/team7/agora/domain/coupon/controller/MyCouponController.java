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
 * 내 쿠폰 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/users/me/coupons")
public class MyCouponController {

    private final CouponQueryService couponQueryService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param couponQueryService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public MyCouponController(CouponQueryService couponQueryService) {
        this.couponQueryService = couponQueryService;
    }

    /**
     * 'getMyCoupons' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param userDetails 인증된 사용자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping
    public ApiResponse<List<MyCouponResponse>> getMyCoupons(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MyCouponResponse> response = couponQueryService.getMyCoupons(userDetails.getUserId());
        return ApiResponse.success("내 쿠폰 목록 조회가 완료되었습니다.", response);
    }
}
