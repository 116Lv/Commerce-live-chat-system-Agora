package com.team7.agora.domain.coupon.entity;

import com.team7.agora.domain.coupon.enums.CouponIssueStatus;
import com.team7.agora.domain.user.entity.User;
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
    name = "coupon_issues",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"coupon_event_id", "user_id"}),
        @UniqueConstraint(columnNames = {"coupon_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_coupon_issues_event_user", columnList = "coupon_event_id, user_id"),
        @Index(name = "idx_coupon_issues_coupon_user", columnList = "coupon_id, user_id"),
        @Index(name = "idx_coupon_issues_user_status", columnList = "user_id, status")
    }
)
public class CouponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_event_id", nullable = false)
    private CouponEvent couponEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponIssueStatus status;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private CouponIssue(Coupon coupon, CouponEvent couponEvent, User user) {
        this.coupon = coupon;
        this.couponEvent = couponEvent;
        this.user = user;
        this.status = CouponIssueStatus.ISSUED;
        this.issuedAt = LocalDateTime.now();
    }

    public static CouponIssue issue(Coupon coupon, CouponEvent couponEvent, User user) {
        return new CouponIssue(coupon, couponEvent, user);
    }
}
