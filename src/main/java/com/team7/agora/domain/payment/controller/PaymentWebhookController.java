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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;
    private final PaymentWebhookVerifier paymentWebhookVerifier;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param paymentWebhookService 입력 값
     * @param paymentWebhookVerifier 입력 값
     */
    public PaymentWebhookController(
        PaymentWebhookService paymentWebhookService,
        PaymentWebhookVerifier paymentWebhookVerifier
    ) {
        this.paymentWebhookService = paymentWebhookService;
        this.paymentWebhookVerifier = paymentWebhookVerifier;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param webhookSecret 입력 값
     * @param request 입력 값
     * @return 처리 결과
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
