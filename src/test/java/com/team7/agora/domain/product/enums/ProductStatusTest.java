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

    @Test
    void displayLabelsAreFriendlyKoreanText() {
        assertThat(ProductStatus.SELLING.getDisplayLabel()).isEqualTo("판매중");
        assertThat(ProductStatus.RESERVED.getDisplayLabel()).isEqualTo("예약중");
        assertThat(ProductStatus.SOLD.getDisplayLabel()).isEqualTo("판매완료");
        assertThat(ProductStatus.HIDDEN.getDisplayLabel()).isEqualTo("숨김");
        assertThat(ProductStatus.DELETED.getDisplayLabel()).isEqualTo("삭제됨");
    }
}
