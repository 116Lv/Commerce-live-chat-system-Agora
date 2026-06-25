package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.Coupon;

public record AdminCouponResponse(
    Long couponId,
    String name,
    int discountAmount,
    int minOrderAmount,
    String type,
    String status,
    int validDays
) {

    public static AdminCouponResponse from(Coupon coupon) {
        return new AdminCouponResponse(
            coupon.getId(),
            coupon.getName(),
            coupon.getDiscountAmount(),
            coupon.getMinOrderAmount(),
            coupon.getType().name(),
            coupon.getStatus().name(),
            coupon.getValidDays()
        );
    }
}
