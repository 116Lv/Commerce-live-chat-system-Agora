package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.global.time.AgoraClock;
import java.time.LocalDateTime;

final class CouponResponseDisplay {

    private CouponResponseDisplay() {
    }

    static int remainingQuantity(CouponEvent event) {
        return Math.max(0, event.getTotalQuantity() - event.getIssuedQuantity());
    }

    static double issueRate(CouponEvent event) {
        if (event.getTotalQuantity() <= 0) {
            return 0.0;
        }
        return (double) event.getIssuedQuantity() / event.getTotalQuantity();
    }

    static boolean canIssue(CouponEvent event) {
        LocalDateTime now = AgoraClock.now();
        return event.getStatus() == CouponEventStatus.ACTIVE
            && !now.isBefore(event.getStartAt())
            && !now.isAfter(event.getEndAt())
            && remainingQuantity(event) > 0;
    }

    static boolean ended(CouponEvent event) {
        return event.getStatus() == CouponEventStatus.ENDED || AgoraClock.now().isAfter(event.getEndAt());
    }

    static boolean soldOut(CouponEvent event) {
        return remainingQuantity(event) == 0;
    }

    static String eventStatusLabel(CouponEvent event) {
        LocalDateTime now = AgoraClock.now();
        if (ended(event)) {
            return "Ended";
        }
        if (soldOut(event)) {
            return "Sold out";
        }
        if (now.isBefore(event.getStartAt())) {
            return "Scheduled";
        }
        return "Active";
    }

    static String eventTypeLabel(CouponEventType type) {
        return switch (type) {
            case FIRST_COME -> "First come";
            case NEW_SIGNUP -> "New signup";
            case ADMIN_INDIVIDUAL -> "Admin individual";
        };
    }

    static String couponStatusLabel(CouponStatus status, LocalDateTime expiresAt) {
        if (isExpired(status, expiresAt)) {
            return "Expired";
        }
        return switch (status) {
            case AVAILABLE -> "Available";
            case ISSUED -> "Issued";
            case USED -> "Used";
            case EXPIRED -> "Expired";
        };
    }

    static boolean isUsable(CouponStatus status, LocalDateTime expiresAt) {
        return status == CouponStatus.ISSUED && !isExpired(status, expiresAt);
    }

    static boolean isExpired(CouponStatus status, LocalDateTime expiresAt) {
        return status == CouponStatus.EXPIRED
            || (expiresAt != null && AgoraClock.now().isAfter(expiresAt));
    }
}
