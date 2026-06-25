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

@RestController
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    public AdminPaymentController(AdminPaymentService adminPaymentService) {
        this.adminPaymentService = adminPaymentService;
    }

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

    @PostMapping("/api/admin/payments/{paymentId}/verify")
    public ApiResponse<AdminPaymentResponse> verifyPayment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long paymentId
    ) {
        AdminPaymentResponse response = adminPaymentService.verifyPayment(userDetails, paymentId);
        return ApiResponse.success("결제 상태를 재검증했습니다.", response);
    }

    @GetMapping("/api/admin/refunds")
    public ApiResponse<List<AdminPaymentResponse>> getRefunds(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success("환불 목록을 조회했습니다.", adminPaymentService.getRefunds(userDetails));
    }
}
