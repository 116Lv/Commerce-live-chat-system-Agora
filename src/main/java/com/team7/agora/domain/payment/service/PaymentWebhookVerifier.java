package com.team7.agora.domain.payment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 결제 웹훅 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Component
public class PaymentWebhookVerifier {

    private final String webhookSecret;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param webhookSecret 웹훅 서명 검증에 사용하는 비밀값
     */
    public PaymentWebhookVerifier(@Value("${portone.webhook-secret:local-webhook-secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param providedSecret 요청 헤더로 전달된 웹훅 비밀값
     * @return 클라이언트에 반환할 API 응답
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
