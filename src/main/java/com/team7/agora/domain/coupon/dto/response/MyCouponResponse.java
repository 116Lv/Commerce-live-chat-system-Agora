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
            coupon.getIssuedAt(),
            coupon.getExpiresAt()
        );
    }
}
