package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminPaymentResponse;
import com.team7.agora.domain.admin.service.AdminPaymentService;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 결제 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminPaymentService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminPaymentController(AdminPaymentService adminPaymentService) {
        this.adminPaymentService = adminPaymentService;
    }

    /**
     * 관리자 결제 정보를 조회하는 GET /api/admin/payments 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param status 조회하거나 변경할 상태
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 항목 개수
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/api/admin/payments")
    public ApiResponse<List<AdminPaymentResponse>> getPayments(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) PaymentStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        List<AdminPaymentResponse> responses = adminPaymentService.getPayments(
            userDetails,
            status,
            PageRequest.of(page, size)
        );
        return ApiResponse.success("결제 목록을 조회했습니다.", responses);
    }

    /**
     * 관리자 결제 기능을 처리하는 POST /api/admin/payments/{paymentId}/verify 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param paymentId 대상 결제 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/api/admin/payments/{paymentId}/verify")
    public ApiResponse<AdminPaymentResponse> verifyPayment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long paymentId
    ) {
        AdminPaymentResponse response = adminPaymentService.verifyPayment(userDetails, paymentId);
        return ApiResponse.success("결제 상태를 재검증했습니다.", response);
    }

    /**
     * 관리자 결제 정보를 조회하는 GET /api/admin/refunds 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/api/admin/refunds")
    public ApiResponse<List<AdminPaymentResponse>> getRefunds(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success("환불 목록을 조회했습니다.", adminPaymentService.getRefunds(userDetails));
    }
}
