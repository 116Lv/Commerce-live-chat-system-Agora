package com.team7.agora.domain.payment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Application service that coordinates payment webhook use cases.
 */
@Component
public class PaymentWebhookVerifier {

    private final String webhookSecret;

    /**
     * Creates a payment webhook verifier instance.
     * @param webhookSecret the webhook secret value
     */
    public PaymentWebhookVerifier(@Value("${portone.webhook-secret:local-webhook-secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    /**
     * Checks whether is valid applies.
     * @param providedSecret the provided secret value
     * @return the is valid result
     */
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
