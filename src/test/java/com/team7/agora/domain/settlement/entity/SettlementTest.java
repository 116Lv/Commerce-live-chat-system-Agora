package com.team7.agora.domain.settlement.entity;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.settlement.enums.SettlementStatus;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.BusinessException;
import jakarta.persistence.JoinColumn;
import java.math.BigDecimal;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SettlementTest {

    private Payment payment;
    private User seller;

    @BeforeEach
    void setUp() {
        seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        User buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        Product product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
        product.markReserved();
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(50000));
        assignId(trade, 100L);
        payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
    }

    @Test
    void pendingCreatesHeldSettlementForSeller() {
        Settlement settlement = Settlement.pending(payment);

        assertThat(settlement.getPayment()).isSameAs(payment);
        assertThat(settlement.getSeller()).isSameAs(seller);
        assertThat(settlement.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.HELD);
    }

    @Test
    void paymentJoinColumnIsUniqueToPreventDuplicateSettlementPerPayment() throws NoSuchFieldException {
        Field paymentField = Settlement.class.getDeclaredField("payment");

        JoinColumn joinColumn = paymentField.getAnnotation(JoinColumn.class);

        assertThat(joinColumn).isNotNull();
        assertThat(joinColumn.unique()).isTrue();
    }

    @Test
    void completeChangesHeldToReady() {
        Settlement settlement = Settlement.pending(payment);

        settlement.complete();

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.READY);
    }

    @Test
    void settleChangesReadyToSettled() {
        Settlement settlement = Settlement.pending(payment);
        settlement.complete();

        settlement.settle();

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.SETTLED);
        assertThat(settlement.getSettledAt()).isNotNull();
    }

    @Test
    void settleRejectsHeldSettlement() {
        Settlement settlement = Settlement.pending(payment);

        assertThatThrownBy(settlement::settle)
            .isInstanceOf(BusinessException.class);
    }
}
