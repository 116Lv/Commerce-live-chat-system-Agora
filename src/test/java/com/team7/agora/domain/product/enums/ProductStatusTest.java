package com.team7.agora.domain.product.enums;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.trade.enums.TradeStatus;
import org.junit.jupiter.api.Test;

class ProductStatusTest {

    @Test
    void statusesDoNotExposeNegotiatingState() {
        assertThat(ProductStatus.values())
            .extracting(Enum::name)
            .doesNotContain("NEGOTIATING");
        assertThat(TradeStatus.values())
            .extracting(Enum::name)
            .doesNotContain("NEGOTIATING");
    }
}
