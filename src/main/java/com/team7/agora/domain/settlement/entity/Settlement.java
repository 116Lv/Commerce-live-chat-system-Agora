package com.team7.agora.domain.settlement.entity;

import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.enums.SettlementStatus;
import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlements")
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    private LocalDateTime settledAt;

    private Settlement(Payment payment) {
        this.payment = payment;
        this.seller = payment.getTrade().getSeller();
        this.amount = payment.getAmount();
        this.status = SettlementStatus.HELD;
        markCreatedNow();
    }

    public static Settlement pending(Payment payment) {
        return new Settlement(payment);
    }

    public void complete() {
        if (status != SettlementStatus.HELD) {
            throw new IllegalStateException("보류 상태의 정산만 정산 가능 상태로 변경할 수 있습니다.");
        }
        status = SettlementStatus.READY;
    }

    public void cancel() {
        if (status != SettlementStatus.HELD && status != SettlementStatus.READY) {
            throw new IllegalStateException("보류 또는 정산 가능 상태의 정산만 취소할 수 있습니다.");
        }
        status = SettlementStatus.FAILED;
    }

    public void settle() {
        if (status != SettlementStatus.READY) {
            throw new IllegalStateException("정산 가능 상태의 정산만 완료할 수 있습니다.");
        }
        status = SettlementStatus.SETTLED;
        settledAt = LocalDateTime.now();
    }
}
