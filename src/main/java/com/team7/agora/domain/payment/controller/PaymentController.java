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

/**
 * REST controller that exposes payment endpoints.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Creates a payment controller instance.
     * @param paymentService the payment service value
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Handles prepare behavior.
     * @param authUser the auth user value
     * @param tradeId the trade id value
     * @return the prepare result
     */
    @PostMapping("/trades/{tradeId}/prepare")
    public ApiResponse<PaymentResponse> prepare(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long tradeId
    ) {
        return prepareResponse(authUser, tradeId);
    }

    /**
     * Handles prepare by request behavior.
     * @param authUser the auth user value
     * @param request the request value
     * @return the prepare by request result
     */
    @PostMapping("/prepare")
    public ApiResponse<PaymentResponse> prepareByRequest(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody PaymentPrepareRequest request
    ) {
        return prepareResponse(authUser, request.tradeId());
    }

    /**
     * Handles confirm behavior.
     * @param authUser the auth user value
     * @param paymentId the payment id value
     * @param request the request value
     * @return the confirm result
     */
    @PostMapping("/{paymentId}/confirm")
    public ApiResponse<PaymentResponse> confirm(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long paymentId,
        @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return confirmResponse(authUser, paymentId, request.paymentKey());
    }

    /**
     * Handles confirm by request behavior.
     * @param authUser the auth user value
     * @param request the request value
     * @return the confirm by request result
     */
    @PostMapping("/confirm")
    public ApiResponse<PaymentResponse> confirmByRequest(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody PaymentConfirmByIdRequest request
    ) {
        return confirmResponse(authUser, request.paymentId(), request.paymentKey());
    }

    /**
     * Handles refund behavior.
     * @param authUser the auth user value
     * @param paymentId the payment id value
     * @param request the request value
     * @return the refund result
     */
    @PostMapping("/{paymentId}/refund")
    public ApiResponse<PaymentResponse> refund(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long paymentId,
        @Valid @RequestBody PaymentRefundRequest request
    ) {
        PaymentResponse response = paymentService.refund(authUser.userId(), paymentId, request.reason());
        return ApiResponse.success("결제가 환불되었습니다.", response);
    }

    /**
     * Returns refund status data.
     * @param authUser the auth user value
     * @param paymentId the payment id value
     * @return the get refund status result
     */
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
