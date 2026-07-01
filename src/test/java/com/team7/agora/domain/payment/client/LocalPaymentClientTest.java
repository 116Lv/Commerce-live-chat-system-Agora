package com.team7.agora.domain.payment.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LocalPaymentClientTest {

    private final LocalPaymentClient client = new LocalPaymentClient();

    @Test
    void confirmsOnlyDeterministicLocalPaymentKeyForOrder() {
        assertThat(client.confirm("local-order-123", "order-123", BigDecimal.valueOf(1000))).isTrue();
        assertThat(client.confirm("anything", "order-123", BigDecimal.valueOf(1000))).isFalse();
    }
}
