package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;

public record MyCouponResponse(
    Long couponId,
    Long eventId,
    String eventName,
    int discountAmount,
    int minOrderAmount,
    String status,
    String statusLabel,
    boolean usable,
    boolean expired,
    LocalDateTime issuedAt,
    LocalDateTime expiresAt
) {

    public static MyCouponResponse from(Coupon coupon) {
        CouponEvent event = coupon.getCouponEvent();
        return new MyCouponResponse(
            coupon.getId(),
            event.getId(),
            event.getName(),
            event.getDiscountAmount(),
            event.getMinOrderAmount(),
            coupon.getStatus().name(),
            CouponResponseDisplay.couponStatusLabel(coupon.getStatus(), coupon.getExpiresAt()),
            CouponResponseDisplay.isUsable(coupon.getStatus(), coupon.getExpiresAt()),
            CouponResponseDisplay.isExpired(coupon.getStatus(), coupon.getExpiresAt()),
            coupon.getIssuedAt(),
            coupon.getExpiresAt()
        );
    }
}
