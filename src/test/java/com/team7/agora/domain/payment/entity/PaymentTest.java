// Payment 엔티티의 DB 제약 매핑을 검증하는 테스트
package com.team7.agora.domain.payment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.JoinColumn;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void tradeJoinColumnIsUniqueToPreventDuplicatePaymentPerTrade() throws NoSuchFieldException {
        Field tradeField = Payment.class.getDeclaredField("trade");

        JoinColumn joinColumn = tradeField.getAnnotation(JoinColumn.class);

        assertThat(joinColumn).isNotNull();
        assertThat(joinColumn.unique()).isTrue();
    }
}
