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

/**
 * Admin Coupon 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminCouponService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminCouponController(AdminCouponService adminCouponService) {
        this.adminCouponService = adminCouponService;
    }

    /**
     * 관리자 쿠폰 생성 요청을 받아 쿠폰 생성 서비스로 전달한다.
     * @param admin 인증된 관리자 정보
     * @param request 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
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

    /**
     * 'getList' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @param couponId 쿠폰 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @GetMapping
    public ApiResponse<List<AdminCouponResponse>> getList(@AuthenticationPrincipal CustomUserDetails admin) {
        List<AdminCouponResponse> response = adminCouponService.getList(admin);
        return ApiResponse.success("쿠폰 정책 목록을 조회했습니다.", response);
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

    /**
     * 조건 충족 여부를 확인한다.
     * @param admin 인증된 관리자 정보
     * @param couponId 쿠폰 ID
     * @param request 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
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

    /**
     * 쿠폰 이벤트에 참여한 사용자들에게 쿠폰 발급 결과를 반환한다.
     * @param admin 인증된 관리자 정보
     * @param couponId 쿠폰 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @PostMapping("/{couponId}/broadcast")
    public ApiResponse<CouponBroadcastResponse> broadcast(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long couponId
    ) {
        CouponBroadcastResponse response = adminCouponService.broadcast(admin, couponId);
        return ApiResponse.success("전체 사용자에게 쿠폰을 발송했습니다.", response);
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
