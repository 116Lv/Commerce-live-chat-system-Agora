package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.Coupon;

/**
 * Response payload for returning admin coupon data.
 * @param couponId the coupon id value
 * @param name the name value
 * @param discountAmount the discount amount value
 * @param minOrderAmount the min order amount value
 * @param type the type value
 * @param status the status value
 * @param validDays the valid days value
 */
public record AdminCouponResponse(
    Long couponId,
    String name,
    int discountAmount,
    int minOrderAmount,
    String type,
    String status,
    int validDays
) {

    /**
     * Creates a response from the given domain object.
     * @param coupon the coupon value
     * @return the from result
     */
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
