package com.team7.agora.domain.payment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Component
public class PaymentWebhookVerifier {

    private final String webhookSecret;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param webhookSecret 입력 값
     */
    public PaymentWebhookVerifier(@Value("${portone.webhook-secret:local-webhook-secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param providedSecret 입력 값
     * @return 처리 결과
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
