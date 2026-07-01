package com.team7.agora.domain.coupon.controller;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.coupon.dto.request.AdminCouponEventCreateRequest;
import com.team7.agora.domain.coupon.dto.request.AdminCouponEventIssueRequest;
import com.team7.agora.domain.coupon.dto.request.AdminCouponEventStopRequest;
import com.team7.agora.domain.coupon.dto.response.AdminCouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.AdminCouponIssueApprovalResponse;
import com.team7.agora.domain.coupon.dto.response.CouponEventCouponResponse;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/coupon-events")
public class AdminCouponEventController {

    private final AdminCouponEventService adminCouponEventService;

    public AdminCouponEventController(AdminCouponEventService adminCouponEventService) {
        this.adminCouponEventService = adminCouponEventService;
    }

    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    @PostMapping("/requests")
    public ApiResponse<AdminApprovalRequestResponse> create(
        @AuthenticationPrincipal AdminPrincipal admin,
        @Valid @RequestBody AdminCouponEventCreateRequest request
    ) {
        AdminApprovalRequestResponse response = adminCouponEventService.createEvent(
            admin,
            request.type(),
            request.name(),
            request.startAt(),
            request.endAt(),
            request.totalQuantity(),
            request.discountAmount(),
            request.minOrderAmount(),
            request.validDays(),
            request.reason()
        );
        return ApiResponse.success("쿠폰 이벤트 생성 승인요청이 등록되었습니다.", response);
    }

    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    @GetMapping
    public ApiResponse<List<AdminCouponEventResponse>> getList(
        @AuthenticationPrincipal AdminPrincipal admin,
        @RequestParam(required = false) CouponEventStatus status
    ) {
        return ApiResponse.success("쿠폰 이벤트 목록을 조회했습니다.", adminCouponEventService.getList(admin, status));
    }

    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    @GetMapping("/{eventId}")
    public ApiResponse<AdminCouponEventResponse> getDetail(
        @AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long eventId
    ) {
        return ApiResponse.success("쿠폰 이벤트 상세를 조회했습니다.", adminCouponEventService.getDetail(admin, eventId));
    }

    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    @PostMapping("/{eventId}/issue-requests")
    public ApiResponse<AdminCouponIssueApprovalResponse> issue(
        @AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long eventId,
        @Valid @RequestBody AdminCouponEventIssueRequest request
    ) {
        return ApiResponse.success(
            "쿠폰 개별발급 승인요청이 등록되었습니다.",
            adminCouponEventService.requestIssueToUsers(admin, eventId, request.userIds())
        );
    }

    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    @PostMapping("/{eventId}/stop-requests")
    public ApiResponse<AdminApprovalRequestResponse> stop(
        @AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long eventId,
        @Valid @RequestBody AdminCouponEventStopRequest request
    ) {
        return ApiResponse.success(
            "쿠폰 이벤트 중단 승인요청이 등록되었습니다.",
            adminCouponEventService.requestStop(admin, eventId, request.reason())
        );
    }

    @PreAuthorize("hasAuthority('COUPON_MANAGE')")
    @GetMapping("/{eventId}/coupons")
    public ApiResponse<List<CouponEventCouponResponse>> getCoupons(
        @AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long eventId
    ) {
        return ApiResponse.success("쿠폰 이벤트 발급 현황을 조회했습니다.", adminCouponEventService.getCoupons(admin, eventId));
    }
}
