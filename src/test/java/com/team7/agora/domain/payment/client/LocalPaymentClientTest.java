package com.team7.agora.domain.payment.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LocalPaymentClientTest {

    private final LocalPaymentClient client = new LocalPaymentClient();

    @Test
    void confirmsAnyNonBlankPaymentKey() {
        assertThat(client.confirm("local-order-123", "order-123", BigDecimal.valueOf(1000))).isTrue();
        assertThat(client.confirm("real-pg-key-xyz", "order-123", BigDecimal.valueOf(1000))).isTrue();
    }

    @Test
    void rejectsBlankOrNullFields() {
        assertThat(client.confirm(null, "order-123", BigDecimal.valueOf(1000))).isFalse();
        assertThat(client.confirm("", "order-123", BigDecimal.valueOf(1000))).isFalse();
        assertThat(client.confirm("key", null, BigDecimal.valueOf(1000))).isFalse();
        assertThat(client.confirm("key", "order-123", BigDecimal.ZERO)).isFalse();
    }
}
