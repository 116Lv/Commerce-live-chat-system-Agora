package com.team7.agora.domain.payment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PaymentWebhookVerifier {

    private final String webhookSecret;

    public PaymentWebhookVerifier(@Value("${portone.webhook-secret:local-webhook-secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public boolean isValid(String providedSecret) {
        if (!StringUtils.hasText(providedSecret)) {
            return false;
        }
        return MessageDigest.isEqual(
            providedSecret.getBytes(StandardCharsets.UTF_8),
            webhookSecret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
