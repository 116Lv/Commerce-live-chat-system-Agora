package com.team7.agora.domain.payment.client;

import java.math.BigDecimal;

/**
 * Client contract for integrating with payment providers.
 */
public interface PaymentClient {

    boolean confirm(String paymentKey, String orderId, BigDecimal amount);
}
