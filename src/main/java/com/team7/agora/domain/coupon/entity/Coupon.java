package com.team7.agora.domain.coupon.entity;

import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.BusinessException;
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
        if (status != CouponStatus.ISSUED) {
            throw new BusinessException(ErrorCode.CONFLICT, "발급된 쿠폰만 사용할 수 있습니다.");
        }
        this.status = CouponStatus.USED;
    }

    public void expire() {
        if (status != CouponStatus.USED) {
            this.status = CouponStatus.EXPIRED;
        }
    }
}
