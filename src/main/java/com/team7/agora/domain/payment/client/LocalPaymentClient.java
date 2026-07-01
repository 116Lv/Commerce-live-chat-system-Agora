package com.team7.agora.domain.payment.client;

import java.math.BigDecimal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 로컬 결제 제공자 연동 구현체이다.
 */
@Component
@Profile({"local", "docker"})
public class LocalPaymentClient implements PaymentClient {
    /**
     * 외부 결제 승인 결과를 검증하고 결제를 완료 상태로 변경한다.
     * @param paymentKey 결제 승인 키
     * @param orderId 주문 ID
     * @param amount 금액
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public boolean confirm(String paymentKey, String orderId, BigDecimal amount) {
        return orderId != null && !orderId.isBlank()
            && paymentKey != null && paymentKey.equals("local-" + orderId)
            && amount != null && amount.signum() > 0;
    }
}
