package com.team7.agora.domain.coupon.controller;

import com.team7.agora.domain.coupon.dto.request.AdminCouponEventCreateRequest;
import com.team7.agora.domain.coupon.dto.request.AdminCouponEventIssueRequest;
import com.team7.agora.domain.coupon.dto.response.AdminCouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.CouponEventCouponResponse;
import com.team7.agora.domain.coupon.dto.response.CouponEventIssueResponse;
import com.team7.agora.domain.coupon.service.AdminCouponEventService;
import com.team7.agora.global.auth.AdminPrincipal;
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
@RequestMapping("/api/admin/coupon-events")
public class AdminCouponEventController {

    private final AdminCouponEventService adminCouponEventService;

    public AdminCouponEventController(AdminCouponEventService adminCouponEventService) {
        this.adminCouponEventService = adminCouponEventService;
    }

    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @PostMapping
    public ApiResponse<AdminCouponEventResponse> create(
        @AuthenticationPrincipal AdminPrincipal admin,
        @Valid @RequestBody AdminCouponEventCreateRequest request
    ) {
        AdminCouponEventResponse response = adminCouponEventService.createEvent(
            admin,
            request.type(),
            request.name(),
            request.startAt(),
            request.endAt(),
            request.totalQuantity(),
            request.discountAmount(),
            request.minOrderAmount(),
            request.validDays()
        );
        return ApiResponse.success("쿠폰 이벤트가 생성되었습니다.", response);
    }

    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @GetMapping
    public ApiResponse<List<AdminCouponEventResponse>> getList(@AuthenticationPrincipal AdminPrincipal admin) {
        return ApiResponse.success("쿠폰 이벤트 목록을 조회했습니다.", adminCouponEventService.getList(admin));
    }

    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @GetMapping("/{eventId}")
    public ApiResponse<AdminCouponEventResponse> getDetail(
        @AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long eventId
    ) {
        return ApiResponse.success("쿠폰 이벤트 상세를 조회했습니다.", adminCouponEventService.getDetail(admin, eventId));
    }

    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @PostMapping("/{eventId}/issue")
    public ApiResponse<CouponEventIssueResponse> issue(
        @AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long eventId,
        @Valid @RequestBody AdminCouponEventIssueRequest request
    ) {
        return ApiResponse.success(
            "지정 사용자에게 쿠폰을 발급했습니다.",
            adminCouponEventService.issueToUsers(admin, eventId, request.userIds())
        );
    }

    @PreAuthorize("hasAnyAuthority('USER_ADMIN', 'ROOT_ADMIN')")
    @GetMapping("/{eventId}/coupons")
    public ApiResponse<List<CouponEventCouponResponse>> getCoupons(
        @AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long eventId
    ) {
        return ApiResponse.success("쿠폰 이벤트 발급 현황을 조회했습니다.", adminCouponEventService.getCoupons(admin, eventId));
    }
}
