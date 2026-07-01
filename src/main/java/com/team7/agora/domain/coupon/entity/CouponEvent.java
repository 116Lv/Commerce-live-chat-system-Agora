package com.team7.agora.domain.coupon.entity;

import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.time.CouponEventTime;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon_events")
public class CouponEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponEventType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minOrderAmount;

    @Column(nullable = false)
    private int validDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponEventStatus status;

    private CouponEvent(
        CouponEventType type,
        String name,
        int totalQuantity,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int discountAmount,
        int minOrderAmount,
        int validDays,
        CouponEventStatus status
    ) {
        markCreatedNow();
        this.type = type;
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
        this.startAt = startAt;
        this.endAt = endAt;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
        this.validDays = validDays;
        this.status = status;
    }

    public static CouponEvent create(
        CouponEventType type,
        String name,
        int totalQuantity,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int discountAmount,
        int minOrderAmount,
        int validDays
    ) {
        return new CouponEvent(
            type,
            name,
            totalQuantity,
            startAt,
            endAt,
            discountAmount,
            minOrderAmount,
            validDays,
            CouponEventStatus.ACTIVE
        );
    }

    public static CouponEvent createPending(
        CouponEventType type,
        String name,
        int totalQuantity,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int discountAmount,
        int minOrderAmount,
        int validDays
    ) {
        return new CouponEvent(
            type,
            name,
            totalQuantity,
            startAt,
            endAt,
            discountAmount,
            minOrderAmount,
            validDays,
            CouponEventStatus.PENDING_APPROVAL
        );
    }

    public void issue() {
        issue(CouponEventTime.now());
    }

    public void issue(LocalDateTime now) {
        validateIssueable(now);
        this.issuedQuantity++;
    }

    public void validateIssueable(LocalDateTime now) {
        if (status != CouponEventStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CONFLICT, "종료된 쿠폰 이벤트입니다.");
        }
        if (now.isBefore(startAt) || now.isAfter(endAt)) {
            throw new BusinessException(ErrorCode.CONFLICT, "쿠폰 이벤트 진행 시간이 아닙니다.");
        }
        if (issuedQuantity >= totalQuantity) {
            throw new BusinessException(ErrorCode.CONFLICT, "쿠폰이 모두 소진되었습니다.");
        }
    }

    public boolean isPublic() {
        return type != CouponEventType.ADMIN_INDIVIDUAL;
    }

    public void approve() {
        if (status != CouponEventStatus.PENDING_APPROVAL) {
            throw invalidStatusTransition("approve");
        }
        this.status = CouponEventStatus.ACTIVE;
    }

    public void reject() {
        if (status != CouponEventStatus.PENDING_APPROVAL) {
            throw invalidStatusTransition("reject");
        }
        this.status = CouponEventStatus.REJECTED;
    }

    public void requestStop() {
        if (status != CouponEventStatus.ACTIVE) {
            throw invalidStatusTransition("request stop");
        }
        this.status = CouponEventStatus.STOP_REQUESTED;
    }

    public void stop() {
        if (status != CouponEventStatus.STOP_REQUESTED) {
            throw invalidStatusTransition("stop");
        }
        this.status = CouponEventStatus.STOPPED;
    }

    public void end() {
        if (status != CouponEventStatus.ACTIVE) {
            throw invalidStatusTransition("end");
        }
        this.status = CouponEventStatus.ENDED;
    }

    private BusinessException invalidStatusTransition(String action) {
        return new BusinessException(ErrorCode.CONFLICT, "Cannot " + action + " coupon event from " + status + ".");
    }
}
