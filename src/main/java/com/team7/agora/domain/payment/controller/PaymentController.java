package com.team7.agora.domain.payment.controller;

import com.team7.agora.domain.payment.dto.request.PaymentConfirmRequest;
import com.team7.agora.domain.payment.dto.request.PaymentConfirmByIdRequest;
import com.team7.agora.domain.payment.dto.request.PaymentPrepareRequest;
import com.team7.agora.domain.payment.dto.request.PaymentRefundRequest;
import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.dto.response.RefundStatusResponse;
import com.team7.agora.domain.payment.service.PaymentService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/trades/{tradeId}/prepare")
    public ApiResponse<PaymentResponse> prepare(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        return prepareResponse(authUser, tradeId);
    }

    @PostMapping("/prepare")
    public ApiResponse<PaymentResponse> prepareByRequest(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody PaymentPrepareRequest request
    ) {
        return prepareResponse(authUser, request.tradeId());
    }

    @PostMapping("/{paymentId}/confirm")
    public ApiResponse<PaymentResponse> confirm(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long paymentId,
        @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return confirmResponse(authUser, paymentId, request.paymentKey());
    }

    @PostMapping("/confirm")
    public ApiResponse<PaymentResponse> confirmByRequest(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody PaymentConfirmByIdRequest request
    ) {
        return confirmResponse(authUser, request.paymentId(), request.paymentKey());
    }

    @PostMapping("/{paymentId}/refund")
    public ApiResponse<PaymentResponse> refund(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long paymentId,
        @Valid @RequestBody PaymentRefundRequest request
    ) {
        PaymentResponse response = paymentService.refund(authUser.userId(), paymentId, request.reason());
        return ApiResponse.success("결제가 환불되었습니다.", response);
    }

    @GetMapping("/{paymentId}/refund")
    public ApiResponse<RefundStatusResponse> getRefundStatus(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long paymentId
    ) {
        RefundStatusResponse response = paymentService.getRefundStatus(authUser, paymentId);
        return ApiResponse.success("환불 상태 조회가 완료되었습니다.", response);
    }

    private ApiResponse<PaymentResponse> prepareResponse(AuthUser authUser, Long tradeId) {
        PaymentResponse response = paymentService.prepare(authUser.userId(), tradeId);
        return ApiResponse.success("결제가 준비되었습니다.", response);
    }

    private ApiResponse<PaymentResponse> confirmResponse(AuthUser authUser, Long paymentId, String paymentKey) {
        PaymentResponse response = paymentService.confirm(authUser.userId(), paymentId, paymentKey);
        return ApiResponse.success("결제가 승인되었습니다.", response);
    }
}
