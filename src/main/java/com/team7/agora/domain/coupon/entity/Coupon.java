package com.team7.agora.domain.coupon.entity;

import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.time.AgoraClock;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "coupons",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"coupon_event_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_coupons_event_status", columnList = "coupon_event_id, status"),
        @Index(name = "idx_coupons_user_status", columnList = "user_id, status")
    }
)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_event_id", nullable = false)
    private CouponEvent couponEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status;

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    private Coupon(CouponEvent couponEvent) {
        this.couponEvent = couponEvent;
        this.status = CouponStatus.AVAILABLE;
    }

    public static Coupon createAvailableSlot(CouponEvent couponEvent) {
        return new Coupon(couponEvent);
    }

    public void assign(User user, LocalDateTime now, int validDays) {
        if (status != CouponStatus.AVAILABLE || this.user != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 발급된 쿠폰 슬롯입니다.");
        }
        this.user = user;
        this.issuedAt = now;
        this.expiresAt = now.plusDays(validDays);
        this.status = CouponStatus.ISSUED;
    }

    public void use() {
        use(AgoraClock.now());
    }

    public void use(LocalDateTime now) {
        if (status != CouponStatus.ISSUED && status != CouponStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "발급된 쿠폰만 사용할 수 있습니다.");
        }
        if (status == CouponStatus.ISSUED && !isUsableAt(now)) {
            throw new BusinessException(ErrorCode.CONFLICT, "만료된 쿠폰은 사용할 수 없습니다.");
        }
        this.status = CouponStatus.USED;
    }

    /**
     * 결제 진행 중 다른 거래에서 같은 쿠폰이 중복 사용되지 않도록 ISSUED -> PAYMENT_PENDING으로 선점한다.
     * 결제가 실패/취소되면 releasePaymentReservation으로 ISSUED로 되돌리고,
     * 결제가 성공하면 use()가 호출되어 USED로 확정된다.
     */
    public void reserveForPayment(Long userId, LocalDateTime now) {
        if (status != CouponStatus.ISSUED) {
            throw new BusinessException(ErrorCode.CONFLICT, "Issued coupon is required for payment.");
        }
        if (user == null || !user.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only the coupon owner can use the coupon.");
        }
        if (status == CouponStatus.ISSUED && !isUsableAt(now)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Expired coupon cannot be used.");
        }
        this.status = CouponStatus.PAYMENT_PENDING;
    }

    public void releasePaymentReservation() {
        if (status == CouponStatus.PAYMENT_PENDING) {
            this.status = CouponStatus.ISSUED;
        }
    }

    public void expire() {
        if (status == CouponStatus.ISSUED || status == CouponStatus.PAYMENT_PENDING) {
            this.status = CouponStatus.EXPIRED;
        }
    }

    public boolean isUsableAt(LocalDateTime now) {
        return status == CouponStatus.ISSUED
            && expiresAt != null
            && !now.isAfter(expiresAt);
    }
}
