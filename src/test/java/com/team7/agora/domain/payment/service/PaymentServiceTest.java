package com.team7.agora.domain.payment.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.payment.client.PaymentClient;
import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.exception.PaymentException;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private PaymentClient paymentClient;

    private PaymentService paymentService;
    private User seller;
    private User buyer;
    private Trade trade;

    @BeforeEach
    void setUp() {
        TransactionOperations transactionOperations = new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(null);
            }
        };
        paymentService = new PaymentService(
            paymentRepository,
            settlementRepository,
            tradeRepository,
            paymentClient,
            transactionOperations
        );
        seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        buyer = User.signup("buyer@test.com", "password", "구매자", "01033334444");
        assignId(buyer, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        Product product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
        product.markReserved();
        trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(50000));
        assignId(trade, 100L);
    }

    @Test
    void prepareCreatesReadyPaymentForBuyer() {
        when(tradeRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(trade));
        when(paymentRepository.existsByTrade(trade)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            assignId(payment, 1000L);
            return payment;
        });

        PaymentResponse response = paymentService.prepare(2L, 100L);

        assertThat(response.paymentId()).isEqualTo(1000L);
        assertThat(response.tradeId()).isEqualTo(100L);
        assertThat(response.payerId()).isEqualTo(2L);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(response.status()).isEqualTo("READY");
    }

    @Test
    void prepareRejectsNonBuyer() {
        when(tradeRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(trade));

        assertThatThrownBy(() -> paymentService.prepare(1L, 100L))
            .isInstanceOf(PaymentException.class);
    }

    @Test
    void prepareRejectsDuplicatePaymentConstraintViolationAsConflict() {
        when(tradeRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(trade));
        when(paymentRepository.existsByTrade(trade)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate payment trade"));

        assertThatThrownBy(() -> paymentService.prepare(2L, 100L))
            .isInstanceOf(PaymentException.class);
    }

    @Test
    void prepareReturnsExistingReadyPaymentForBuyerWhenCheckoutReloads() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        when(tradeRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(trade));
        when(paymentRepository.findByTrade(trade)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.prepare(2L, 100L);

        assertThat(response.paymentId()).isEqualTo(1000L);
        assertThat(response.status()).isEqualTo("READY");
        assertThat(response.orderId()).isEqualTo("order-1");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void prepareReturnsExistingConfirmingPaymentForBuyerWhenProviderReturnsToCheckout() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        payment.markConfirming();
        when(tradeRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(trade));
        when(paymentRepository.findByTrade(trade)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.prepare(2L, 100L);

        assertThat(response.paymentId()).isEqualTo(1000L);
        assertThat(response.status()).isEqualTo("CONFIRMING");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void confirmExistingConfirmingPaymentThrowsConflictWithoutCallingPaymentClientAgain() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        payment.markConfirming();
        when(tradeRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(trade));
        when(paymentRepository.findByTrade(trade)).thenReturn(Optional.of(payment));
        when(paymentRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(payment));

        PaymentResponse prepared = paymentService.prepare(2L, 100L);

        assertThat(prepared.status()).isEqualTo("CONFIRMING");
        assertThatThrownBy(() -> paymentService.confirm(2L, prepared.paymentId(), "payment-key"))
            .isInstanceOfSatisfying(PaymentException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONFLICT)
            )
            .hasMessageContaining("approval in progress");
        verify(paymentClient, never()).confirm(any(), any(), any());
        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    void confirmMarksPaidAndCreatesSettlement() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        when(paymentRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(payment), Optional.of(payment));
        when(paymentClient.confirm("payment-key", "order-1", BigDecimal.valueOf(50000))).thenAnswer(invocation -> {
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CONFIRMING);
            return true;
        });
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> {
            Settlement settlement = invocation.getArgument(0);
            assignId(settlement, 2000L);
            return settlement;
        });

        PaymentResponse response = paymentService.confirm(2L, 1000L, "payment-key");

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.paymentKey()).isEqualTo("payment-key");
        assertThat(response.settlementId()).isEqualTo(2000L);
    }

    @Test
    void confirmRestoresReadyWhenPaymentClientThrows() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        when(paymentRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(payment), Optional.of(payment));
        when(paymentClient.confirm("payment-key", "order-1", BigDecimal.valueOf(50000)))
            .thenThrow(new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PG confirm failed"));

        assertThatThrownBy(() -> paymentService.confirm(2L, 1000L, "payment-key"))
            .isInstanceOf(PaymentException.class);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    void confirmAlreadyPaidPaymentReturnsWithoutCallingPaymentClientAgain() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        payment.markPaid("payment-key");
        trade.markPaid();
        when(paymentRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.confirm(2L, 1000L, "payment-key");

        assertThat(response.status()).isEqualTo("PAID");
        verify(paymentClient, never()).confirm(any(), any(), any());
        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    void refundMarksPaidPaymentRefunded() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        payment.markPaid("payment-key");
        when(paymentRepository.findById(1000L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.refund(2L, 1000L, "구매자 요청");

        assertThat(response.status()).isEqualTo("REFUNDED");
    }

    @Test
    void getRefundStatusReturnsStatusForParticipant() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        payment.markPaid("payment-key");
        payment.refund("구매자 요청");
        when(paymentRepository.findById(1000L)).thenReturn(Optional.of(payment));

        var response = paymentService.getRefundStatus(new AuthUser(2L, "buyer@test.com", "ROLE_USER", "구매자"), 1000L);

        assertThat(response.status()).isEqualTo("REFUNDED");
        assertThat(response.refundedAt()).isNotNull();
    }

    @Test
    void getRefundStatusAllowsSettlementAdmin() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        when(paymentRepository.findById(1000L)).thenReturn(Optional.of(payment));

        var response = paymentService.getRefundStatus(
            new AuthUser(99L, "settlement@admin.com", "SETTLEMENT_ADMIN", "정산관리자"), 1000L
        );

        assertThat(response.status()).isEqualTo("READY");
    }

    @Test
    void getRefundStatusRejectsNonParticipantNonAdmin() {
        Payment payment = Payment.ready(trade, buyer, BigDecimal.valueOf(50000), "order-1");
        assignId(payment, 1000L);
        when(paymentRepository.findById(1000L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getRefundStatus(
            new AuthUser(3L, "stranger@test.com", "ROLE_USER", "제3자"), 1000L
        )).isInstanceOf(PaymentException.class);
    }
}
