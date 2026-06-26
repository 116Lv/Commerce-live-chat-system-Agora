package com.team7.agora.domain.payment.client;

import java.math.BigDecimal;

/**
 * 결제 제공자 연동을 위한 클라이언트 계약이다.
 */
public interface PaymentClient {

    boolean confirm(String paymentKey, String orderId, BigDecimal amount);
}
