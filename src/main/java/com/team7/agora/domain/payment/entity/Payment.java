package com.team7.agora.domain.payment.entity;

import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.exception.PaymentException;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_payment_key", columnList = "payment_key"),
        @Index(name = "idx_payments_status_requested", columnList = "status, requested_at")
    }
/**
 * JPA entity that represents a payment record.
 */
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;

    @Column(nullable = false, unique = true, length = 80)
    private String orderId;

    @Column(length = 120)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    private Payment(Trade trade, User payer, BigDecimal amount, String orderId) {
        this.trade = trade;
        this.payer = payer;
        this.amount = amount;
        this.orderId = orderId;
        this.status = PaymentStatus.READY;
        this.requestedAt = LocalDateTime.now();
    }

    /**
     * Handles ready behavior.
     * @param trade the trade value
     * @param payer the payer value
     * @param amount the amount value
     * @param orderId the order id value
     * @return the ready result
     */
    public static Payment ready(Trade trade, User payer, BigDecimal amount, String orderId) {
        return new Payment(trade, payer, amount, orderId);
    }

    /**
     * Validates payer rules.
     * @param userId the user id value
     */
    public void validatePayer(Long userId) {
        if (!payer.getId().equals(userId)) {
            throw new PaymentException(ErrorCode.FORBIDDEN, "결제 당사자만 처리할 수 있습니다.");
        }
    }

    /**
     * Marks paid state.
     * @param paymentKey the payment key value
     */
    public void markPaid(String paymentKey) {
        if (status == PaymentStatus.PAID) {
            return;
        }
        if (status != PaymentStatus.READY) {
            throw new PaymentException(ErrorCode.CONFLICT, "결제 대기 상태에서만 승인할 수 있습니다.");
        }
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Handles refund behavior.
     * @param reason the reason value
     */
    public void refund(String reason) {
        if (status != PaymentStatus.PAID) {
            throw new PaymentException(ErrorCode.CONFLICT, "결제 완료 상태에서만 환불할 수 있습니다.");
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    /**
     * Marks failed state.
     */
    public void markFailed() {
        if (status == PaymentStatus.PAID) {
            throw new PaymentException(ErrorCode.CONFLICT, "결제완료 상태는 실패로 변경할 수 없습니다.");
        }
        this.status = PaymentStatus.FAILED;
    }
}
