package com.team7.agora.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.agora.domain.payment.dto.request.PaymentWebhookRequest;
import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.exception.PaymentException;
import com.team7.agora.domain.payment.service.PaymentWebhookService;
import com.team7.agora.domain.payment.service.PaymentWebhookVerifier;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.response.ApiResponse;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receives PortOne payment webhooks and verifies them before state changes.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;
    private final PaymentWebhookVerifier paymentWebhookVerifier;
    private final ObjectMapper objectMapper;

    public PaymentWebhookController(
        PaymentWebhookService paymentWebhookService,
        PaymentWebhookVerifier paymentWebhookVerifier,
        ObjectMapper objectMapper
    ) {
        this.paymentWebhookService = paymentWebhookService;
        this.paymentWebhookVerifier = paymentWebhookVerifier;
        this.objectMapper = objectMapper;
    }

    @PostMapping({"/webhook", "/webhooks/portone"})
    public ResponseEntity<ApiResponse<PaymentResponse>> portOneWebhook(
        @RequestHeader(name = "PortOne-Webhook-Timestamp", required = false) String timestamp,
        @RequestHeader(name = "PortOne-Webhook-Signature", required = false) String signature,
        @RequestBody String body
    ) {
        if (!paymentWebhookVerifier.isValid(body, timestamp, signature)) {
            throw new PaymentException(ErrorCode.UNAUTHORIZED, "결제 웹훅 서명 검증에 실패했습니다.");
        }

        PaymentWebhookRequest request = parseRequest(body);
        if (!"PAID".equalsIgnoreCase(request.status())) {
            throw new PaymentException(ErrorCode.INVALID_REQUEST, "지원하지 않는 결제 웹훅 상태입니다.");
        }

        PaymentResponse response = paymentWebhookService.handlePaid(request.orderId(), request.paymentKey());
        return ResponseEntity.ok(ApiResponse.success("결제 웹훅을 처리했습니다.", response));
    }

    private PaymentWebhookRequest parseRequest(String body) {
        try {
            return objectMapper.readValue(body, PaymentWebhookRequest.class);
        } catch (IOException e) {
            throw new PaymentException(ErrorCode.INVALID_REQUEST, "결제 웹훅 본문을 해석할 수 없습니다.");
        }
    }
}
