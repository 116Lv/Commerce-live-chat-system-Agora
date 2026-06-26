package com.team7.agora.domain.payment.controller;

import com.team7.agora.domain.payment.dto.request.PaymentWebhookRequest;
import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.exception.PaymentException;
import com.team7.agora.domain.payment.service.PaymentWebhookService;
import com.team7.agora.domain.payment.service.PaymentWebhookVerifier;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 결제 웹훅 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;
    private final PaymentWebhookVerifier paymentWebhookVerifier;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param paymentWebhookService 해당 기능의 비즈니스 로직을 처리하는 서비스
     * @param paymentWebhookVerifier 결제 웹훅 서명을 검증하는 컴포넌트
     */
    public PaymentWebhookController(
        PaymentWebhookService paymentWebhookService,
        PaymentWebhookVerifier paymentWebhookVerifier
    ) {
        this.paymentWebhookService = paymentWebhookService;
        this.paymentWebhookVerifier = paymentWebhookVerifier;
    }

    /**
     * 결제 웹훅 기능을 처리하는 POST /api/payments 요청을 처리한다.
     * @param webhookSecret 웹훅 서명 검증에 사용하는 비밀값
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping({"/webhook", "/webhooks/portone"})
    public ResponseEntity<ApiResponse<PaymentResponse>> portOneWebhook(
        @RequestHeader(name = "PortOne-Webhook-Secret", required = false) String webhookSecret,
        @Valid @RequestBody PaymentWebhookRequest request
    ) {
        if (!paymentWebhookVerifier.isValid(webhookSecret)) {
            throw new PaymentException(ErrorCode.UNAUTHORIZED, "결제 웹훅 서명 검증에 실패했습니다.");
        }
        if (!"PAID".equalsIgnoreCase(request.status())) {
            throw new PaymentException(ErrorCode.INVALID_REQUEST, "지원하지 않는 결제 웹훅 상태입니다.");
        }

        PaymentResponse response = paymentWebhookService.handlePaid(request.orderId(), request.paymentKey());
        return ResponseEntity.ok(ApiResponse.success("결제 웹훅이 처리되었습니다.", response));
    }
}
