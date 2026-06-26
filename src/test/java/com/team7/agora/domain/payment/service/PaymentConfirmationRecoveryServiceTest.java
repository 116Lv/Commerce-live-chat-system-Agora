package com.team7.agora.domain.payment.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.user.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmationRecoveryServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentConfirmationRecoveryService recoveryService;
    private User seller;
    private User buyer;
    private Trade trade;

    @BeforeEach
    void setUp() {
        recoveryService = new PaymentConfirmationRecoveryService(paymentRepository);

        seller = User.signup("seller@test.com", "password", "seller", "01011112222");
        assignId(seller, 1L);
        buyer = User.signup("buyer@test.com", "password", "buyer", "01033334444");
        assignId(buyer, 2L);
        Region region = Region.create("Seoul Gangnam", "1168010100", "Seoul", "Gangnam", "Yeoksam");
        Product product = Product.create(seller, region, "bike", "good", BigDecimal.valueOf(50000), "sports");
        assignId(product, 10L);
        product.markReserved();
        trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(50000));
        assignId(trade, 100L);
    }

    @Test
    void recoversStaleConfirmingPaymentsToReady() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        payment.markConfirming();
        LocalDateTime threshold = LocalDateTime.of(2026, 6, 26, 12, 0);
        when(paymentRepository.findAllByStatusAndConfirmingAtLessThanEqualForUpdate(
            PaymentStatus.CONFIRMING,
            threshold
        )).thenReturn(List.of(payment));

        int recoveredCount = recoveryService.recoverStaleConfirmingPayments(threshold);

        assertThat(recoveredCount).isEqualTo(1);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
    }
}
