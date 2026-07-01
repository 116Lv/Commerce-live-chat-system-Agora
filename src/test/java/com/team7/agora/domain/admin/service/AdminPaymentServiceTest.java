package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.payment.service.PaymentService;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.enums.SettlementStatus;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminPaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SettlementRepository settlementRepository;

    private AdminPaymentService adminPaymentService;
    private AdminPrincipal settlementAdmin;

    @BeforeEach
    void setUp() {
        adminPaymentService = new AdminPaymentService(paymentRepository, paymentService, settlementRepository);
        settlementAdmin = new AdminPrincipal(
            99L,
            "settlement@admin.com",
            "encoded",
            AdminRole.SETTLEMENT_ADMIN,
            AdminStatus.ACTIVE,
            "정산관리자"
        );
    }

    @Test
    void getPaymentsAllowsSettlementAdmin() {
        Payment payment = payment(1L, PaymentStatus.PAID);
        Settlement settlement = org.mockito.Mockito.mock(Settlement.class);
        when(paymentRepository.findAll(PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(payment)));
        when(settlementRepository.findByPayment(payment)).thenReturn(Optional.of(settlement));
        when(settlement.getId()).thenReturn(10L);
        when(settlement.getStatus()).thenReturn(SettlementStatus.READY);

        var responses = adminPaymentService.getPayments(settlementAdmin, null, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().paymentId()).isEqualTo(1L);
        assertThat(responses.getFirst().settlementId()).isEqualTo(10L);
        assertThat(responses.getFirst().settlementStatus()).isEqualTo("READY");
        assertThat(responses.getFirst().status()).isEqualTo("PAID");
    }

    @Test
    void getRefundsReturnsRefundedPaymentsOnly() {
        Payment refunded = payment(2L, PaymentStatus.REFUNDED);
        when(paymentRepository.findAllByStatus(PaymentStatus.REFUNDED)).thenReturn(List.of(refunded));

        var responses = adminPaymentService.getRefunds(settlementAdmin);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().status()).isEqualTo("REFUNDED");
    }

    @Test
    void verifyPaymentFallsBackToOrderIdWhenPaymentKeyIsMissing() {
        Payment ready = payment(3L, PaymentStatus.READY);
        when(ready.getPaymentKey()).thenReturn(null);
        when(paymentRepository.findById(3L)).thenReturn(Optional.of(ready));

        adminPaymentService.verifyPayment(settlementAdmin, 3L);

        verify(paymentService).confirmByPaymentId(3L, "order-3");
    }

    @Test
    void verifyPaymentUsesSharedPaymentConfirmationFlow() {
        Payment ready = payment(4L, PaymentStatus.READY);
        when(paymentRepository.findById(4L)).thenReturn(Optional.of(ready));

        adminPaymentService.verifyPayment(settlementAdmin, 4L);

        verify(paymentService).confirmByPaymentId(4L, "payment-key-4");
    }

    @Test
    void verifyPaymentReloadsPaymentAfterConfirmationBeforeBuildingAdminResponse() {
        Payment ready = payment(5L, PaymentStatus.READY);
        Payment paid = payment(5L, PaymentStatus.PAID);
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(ready), Optional.of(paid));

        var response = adminPaymentService.verifyPayment(settlementAdmin, 5L);

        assertThat(response.status()).isEqualTo("PAID");
        verify(paymentService).confirmByPaymentId(5L, "payment-key-5");
        verify(settlementRepository).findByPayment(paid);
    }

    @Test
    void wrongAdminRoleCannotReadPayments() {
        AdminPrincipal user = new AdminPrincipal(
            1L,
            "user-admin@test.com",
            "encoded",
            AdminRole.USER_ADMIN,
            AdminStatus.ACTIVE,
            "사용자관리자"
        );

        assertThatThrownBy(() -> adminPaymentService.getPayments(user, null, PageRequest.of(0, 20)))
            .isInstanceOf(BusinessException.class);
    }

    private Payment payment(Long id, PaymentStatus status) {
        Payment payment = org.mockito.Mockito.mock(Payment.class);
        lenient().when(payment.getId()).thenReturn(id);
        lenient().when(payment.getAmount()).thenReturn(BigDecimal.valueOf(50000));
        lenient().when(payment.getOrderId()).thenReturn("order-" + id);
        lenient().when(payment.getPaymentKey()).thenReturn("payment-key-" + id);
        lenient().when(payment.getStatus()).thenReturn(status);
        lenient().when(payment.getRequestedAt()).thenReturn(LocalDateTime.now());
        return payment;
    }
}
