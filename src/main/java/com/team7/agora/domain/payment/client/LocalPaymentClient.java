package com.team7.agora.domain.payment.client;

import java.math.BigDecimal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalPaymentClient implements PaymentClient {

    @Override
    public boolean confirm(String paymentKey, String orderId, BigDecimal amount) {
        return paymentKey != null && !paymentKey.isBlank()
            && orderId != null && !orderId.isBlank()
            && amount != null && amount.signum() > 0;
    }
}
