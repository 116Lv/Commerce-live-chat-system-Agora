package com.team7.agora.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PaymentWebhookVerifierTest {

    @Test
    void validHmacSignatureWithinTimestampToleranceIsAccepted() {
        PaymentWebhookVerifier verifier = new PaymentWebhookVerifier("secret");
        String body = "{\"orderId\":\"order-1\",\"paymentKey\":\"payment-key\",\"status\":\"PAID\"}";
        long timestamp = 1_800_000_000L;
        String signature = PaymentWebhookVerifier.createSignature("secret", timestamp, body);

        assertThat(verifier.isValid(body, String.valueOf(timestamp), signature, timestamp + 60)).isTrue();
    }

    @Test
    void staleTimestampIsRejectedEvenWithValidSignature() {
        PaymentWebhookVerifier verifier = new PaymentWebhookVerifier("secret");
        String body = "{\"orderId\":\"order-1\",\"paymentKey\":\"payment-key\",\"status\":\"PAID\"}";
        long timestamp = 1_800_000_000L;
        String signature = PaymentWebhookVerifier.createSignature("secret", timestamp, body);

        assertThat(verifier.isValid(body, String.valueOf(timestamp), signature, timestamp + 600)).isFalse();
    }
}
