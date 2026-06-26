package com.team7.agora.domain.payment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Verifies PortOne webhook signatures with a bounded timestamp tolerance.
 */
@Component
public class PaymentWebhookVerifier {

    private static final Duration TIMESTAMP_TOLERANCE = Duration.ofMinutes(5);

    private final String webhookSecret;

    public PaymentWebhookVerifier(@Value("${portone.webhook-secret:local-webhook-secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public boolean isValid(String body, String timestampHeader, String signatureHeader) {
        return isValid(body, timestampHeader, signatureHeader, System.currentTimeMillis() / 1000);
    }

    public boolean isValid(String body, String timestampHeader, String signatureHeader, long nowEpochSeconds) {
        if (!StringUtils.hasText(body)
            || !StringUtils.hasText(timestampHeader)
            || !StringUtils.hasText(signatureHeader)
            || !StringUtils.hasText(webhookSecret)) {
            return false;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader);
        } catch (NumberFormatException e) {
            return false;
        }

        if (Math.abs(nowEpochSeconds - timestamp) > TIMESTAMP_TOLERANCE.toSeconds()) {
            return false;
        }

        String expected = createSignature(webhookSecret, timestamp, body);
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            signatureHeader.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String createSignature(String secret, long timestamp, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create webhook signature.", e);
        }
    }
}
