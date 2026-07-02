package com.team7.agora.domain.payment.controller;

import com.team7.agora.domain.payment.dto.request.PaymentConfirmRequest;
import com.team7.agora.domain.payment.dto.request.PaymentConfirmByIdRequest;
import com.team7.agora.domain.payment.dto.request.PaymentPrepareRequest;
import com.team7.agora.domain.payment.dto.request.PaymentRefundRequest;
import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.dto.response.RefundStatusResponse;
import com.team7.agora.domain.payment.service.PaymentService;
import com.team7.agora.global.auth.CustomUserDetails;
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
 * 결제 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param paymentService 결제 비즈니스 로직을 처리하는 서비스
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 결제 정보를 생성하거나 준비하는 POST /api/payments/trades/{tradeId}/prepare 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param tradeId 대상 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/trades/{tradeId}/prepare")
    public ApiResponse<PaymentResponse> prepare(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long tradeId
    ) {
        return prepareResponse(userDetails, tradeId);
    }

    /**
     * 결제 정보를 생성하거나 준비하는 POST /api/payments/prepare 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/prepare")
    public ApiResponse<PaymentResponse> prepareByRequest(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody PaymentPrepareRequest request
    ) {
        return prepareResponse(userDetails, request.tradeId(), request.couponId());
    }

    /**
     * 결제를 확정하거나 진행하는 POST /api/payments/{paymentId}/confirm 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param paymentId 대상 결제 ID
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{paymentId}/confirm")
    public ApiResponse<PaymentResponse> confirm(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long paymentId,
        @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return confirmResponse(userDetails, paymentId, request.paymentKey());
    }

    /**
     * 결제를 확정하거나 진행하는 POST /api/payments/confirm 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/confirm")
    public ApiResponse<PaymentResponse> confirmByRequest(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody PaymentConfirmByIdRequest request
    ) {
        return confirmResponse(userDetails, request.paymentId(), request.paymentKey());
    }

    /**
     * 결제 상태를 변경하는 POST /api/payments/{paymentId}/refund 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param paymentId 대상 결제 ID
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{paymentId}/refund")
    public ApiResponse<PaymentResponse> refund(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long paymentId,
        @Valid @RequestBody PaymentRefundRequest request
    ) {
        PaymentResponse response = paymentService.refund(userDetails.getUserId(), paymentId, request.reason());
        return ApiResponse.success("결제가 환불되었습니다.", response);
    }

    /**
     * 결제 정보를 조회하는 GET /api/payments/{paymentId}/refund 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param paymentId 대상 결제 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/{paymentId}/refund")
    public ApiResponse<RefundStatusResponse> getRefundStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long paymentId
    ) {
        RefundStatusResponse response = paymentService.getRefundStatus(userDetails.toAuthUser(), paymentId);
        return ApiResponse.success("환불 상태 조회가 완료되었습니다.", response);
    }

    private ApiResponse<PaymentResponse> prepareResponse(CustomUserDetails userDetails, Long tradeId) {
        PaymentResponse response = paymentService.prepare(userDetails.getUserId(), tradeId, null);
        return ApiResponse.success("결제가 준비되었습니다.", response);
    }

    private ApiResponse<PaymentResponse> prepareResponse(CustomUserDetails userDetails, Long tradeId, Long couponId) {
        PaymentResponse response = paymentService.prepare(userDetails.getUserId(), tradeId, couponId);
        return ApiResponse.success("Payment prepared.", response);
    }

    private ApiResponse<PaymentResponse> confirmResponse(CustomUserDetails userDetails, Long paymentId, String paymentKey) {
        PaymentResponse response = paymentService.confirm(userDetails.getUserId(), paymentId, paymentKey);
        return ApiResponse.success("결제가 승인되었습니다.", response);
    }
}
