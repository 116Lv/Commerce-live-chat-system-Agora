package com.team7.agora.domain.payment.client;

import java.math.BigDecimal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Client implementation for integrating with local payment providers.
 */
@Component
@Profile("local")
public class LocalPaymentClient implements PaymentClient {
    /**
     * Handles confirm behavior.
     * @param paymentKey the payment key value
     * @param orderId the order id value
     * @param amount the amount value
     * @return the confirm result
     */
    @Override
    public boolean confirm(String paymentKey, String orderId, BigDecimal amount) {
        return paymentKey != null && !paymentKey.isBlank()
            && orderId != null && !orderId.isBlank()
            && amount != null && amount.signum() > 0;
    }
}
