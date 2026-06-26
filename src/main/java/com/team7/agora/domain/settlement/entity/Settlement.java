package com.team7.agora.domain.settlement.entity;

import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.settlement.enums.SettlementStatus;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.time.AgoraClock;
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

/**
 * Settlement 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlements")
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
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

    /**
     * 'pending' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param payment 결제 엔티티 또는 결제 응답 변환 대상
     * @return 클라이언트에 반환할 API 응답
     */
    public static Settlement pending(Payment payment) {
        return new Settlement(payment);
    }

    /**
     * 도메인 객체를 완료 상태로 변경한다.
     */
    public void complete() {
        if (status != SettlementStatus.HELD) {
            throw new IllegalStateException("보류 상태의 정산만 정산 가능 상태로 변경할 수 있습니다.");
        }
        status = SettlementStatus.READY;
    }

    /**
     * 도메인 객체를 취소 상태로 변경한다.
     */
    public void cancel() {
        if (status != SettlementStatus.HELD && status != SettlementStatus.READY) {
            throw new IllegalStateException("보류 또는 정산 가능 상태의 정산만 취소할 수 있습니다.");
        }
        status = SettlementStatus.FAILED;
    }

    /**
     * 'settle' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     */
    public void settle() {
        if (status != SettlementStatus.READY) {
            throw new IllegalStateException("정산 가능 상태의 정산만 완료할 수 있습니다.");
        }
        status = SettlementStatus.SETTLED;
        settledAt = AgoraClock.now();
    }
}
