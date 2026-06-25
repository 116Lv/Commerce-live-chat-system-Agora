package com.team7.agora.domain.payment.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.user.entity.User;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SettlementRepository settlementRepository;

    private PaymentWebhookService paymentWebhookService;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentWebhookService = new PaymentWebhookService(paymentRepository, settlementRepository);
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        User buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋아요",
            BigDecimal.valueOf(50000),
            "스포츠"
        );
        assignId(product, 10L);
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(50000));
        assignId(trade, 100L);
        payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
    }

    @Test
    void paidWebhookMarksPaymentPaidAndCreatesSettlement() {
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));
        when(settlementRepository.existsByPayment(payment)).thenReturn(false);
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> {
            Settlement settlement = invocation.getArgument(0);
            assignId(settlement, 2000L);
            return settlement;
        });

        PaymentResponse response = paymentWebhookService.handlePaid("order-1", "payment-key");

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.settlementId()).isEqualTo(2000L);
    }

    @Test
    void duplicatedPaidWebhookDoesNotCreateSettlementAgain() {
        payment.markPaid("payment-key");
        payment.getTrade().markPaid();
        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));
        when(settlementRepository.existsByPayment(payment)).thenReturn(true);

        PaymentResponse response = paymentWebhookService.handlePaid("order-1", "payment-key");

        assertThat(response.status()).isEqualTo("PAID");
        verify(settlementRepository, never()).save(any(Settlement.class));
    }
}
