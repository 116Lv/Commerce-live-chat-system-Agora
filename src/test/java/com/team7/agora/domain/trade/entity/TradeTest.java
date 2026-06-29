package com.team7.agora.domain.trade.entity;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TradeTest {

    private Trade trade;

    @BeforeEach
    void setUp() {
        User seller = User.signup("seller@test.com", "password", "seller", "01011112222");
        assignId(seller, 1L);
        User buyer = User.signup("buyer@test.com", "password", "buyer", "01033334444");
        assignId(buyer, 2L);
        Region region = Region.create("region", "1168010100", "city", "district", "dong");
        Product product = Product.create(seller, region, "product", "description", BigDecimal.valueOf(50000), "image");
        assignId(product, 10L);
        trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(50000));
    }

    @Test
    void completeRejectsPaymentPendingTradeAsBusinessException() {
        assertThatThrownBy(trade::complete)
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelRejectsCompletedTradeAsBusinessException() {
        trade.markPaid();
        trade.complete();

        assertThatThrownBy(trade::cancel)
                .isInstanceOf(BusinessException.class);
        assertThat(trade.getProduct().getStatus()).isEqualTo(ProductStatus.SOLD);
    }

    @Test
    void cancelChangesPaymentPendingTradeStatusToCancelledAndProductStatusToSelling() {
        trade.getProduct().markReserved();

        trade.cancel();

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.CANCELLED);
        assertThat(trade.getProduct().getStatus()).isEqualTo(ProductStatus.SELLING);
    }

    @Test
    void cancelRejectsPaidTradeAndKeepsSoldProductStatus() {
        trade.markPaid();

        assertThatThrownBy(trade::cancel)
                .isInstanceOf(BusinessException.class);
        assertThat(trade.getProduct().getStatus()).isEqualTo(ProductStatus.SOLD);
    }

    @Test
    void expireRejectsSoldProductAndKeepsProductStatus() {
        trade.getProduct().markSold();

        assertThatThrownBy(trade::expire)
                .isInstanceOf(BusinessException.class);
        assertThat(trade.getProduct().getStatus()).isEqualTo(ProductStatus.SOLD);
    }
}
