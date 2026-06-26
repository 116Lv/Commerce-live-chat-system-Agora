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

/**
 * 쿠폰 이벤트 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/coupon-events")
public class CouponEventController {

    private final CouponIssueService couponIssueService;
    private final CouponQueryService couponQueryService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param couponIssueService 해당 기능의 비즈니스 로직을 처리하는 서비스
     * @param couponQueryService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public CouponEventController(CouponIssueService couponIssueService, CouponQueryService couponQueryService) {
        this.couponIssueService = couponIssueService;
        this.couponQueryService = couponQueryService;
    }

    /**
     * 쿠폰 이벤트 정보를 조회하는 GET /api/coupon-events 요청을 처리한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping
    public ApiResponse<List<CouponEventResponse>> list() {
        return ApiResponse.success("진행중인 쿠폰 이벤트 목록입니다.", couponQueryService.listActiveEvents());
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param userDetails 인증된 사용자 정보
     * @param eventId 이벤트 ID
     * @return 클라이언트에 반환할 API 응답
     */
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
