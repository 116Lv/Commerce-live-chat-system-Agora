package com.team7.agora.domain.payment.client;

import java.math.BigDecimal;

public interface PaymentClient {

    boolean confirm(String paymentKey, String orderId, BigDecimal amount);
}
