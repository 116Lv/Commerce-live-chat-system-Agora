package com.team7.agora.domain.payment.client;

import java.math.BigDecimal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 로컬 결제 제공자 연동 구현체이다.
 */
@Component
@Profile("local")
public class LocalPaymentClient implements PaymentClient {
    /**
     * 요청한 동작을 처리한다.
     * @param paymentKey 입력 값
     * @param orderId 입력 값
     * @param amount 입력 값
     * @return 처리 결과
     */
    @Override
    public boolean confirm(String paymentKey, String orderId, BigDecimal amount) {
        return paymentKey != null && !paymentKey.isBlank()
            && orderId != null && !orderId.isBlank()
            && amount != null && amount.signum() > 0;
    }
}
