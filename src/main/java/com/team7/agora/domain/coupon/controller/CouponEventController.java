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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/coupon-events")
public class CouponEventController {

    private final CouponIssueService couponIssueService;
    private final CouponQueryService couponQueryService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param couponIssueService 입력 값
     * @param couponQueryService 입력 값
     */
    public CouponEventController(CouponIssueService couponIssueService, CouponQueryService couponQueryService) {
        this.couponIssueService = couponIssueService;
        this.couponQueryService = couponQueryService;
    }

    /**
     * 요청한 동작을 처리한다.
     * @return 처리 결과
     */
    @GetMapping
    public ApiResponse<List<CouponEventResponse>> list() {
        return ApiResponse.success("진행중인 쿠폰 이벤트 목록입니다.", couponQueryService.listActiveEvents());
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param userDetails 입력 값
     * @param eventId 입력 값
     * @return 처리 결과
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
