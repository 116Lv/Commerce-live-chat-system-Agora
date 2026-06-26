package com.team7.agora.domain.payment.service;

import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles payment provider webhook events through the same confirmation path
 * used by user and admin payment confirmation.
 */
@Service
@Transactional(readOnly = true)
public class PaymentWebhookService {

    private final PaymentService paymentService;

    public PaymentWebhookService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Transactional
    public PaymentResponse handlePaid(String orderId, String paymentKey) {
        return paymentService.confirmByOrderId(orderId, paymentKey);
    }
}
