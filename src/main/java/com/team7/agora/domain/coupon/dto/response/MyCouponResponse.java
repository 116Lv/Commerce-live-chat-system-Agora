package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponIssue;
import java.time.LocalDateTime;

/**
 * Response payload for returning my coupon data.
 * @param issueId the issue id value
 * @param couponId the coupon id value
 * @param couponName the coupon name value
 * @param discountAmount the discount amount value
 * @param minOrderAmount the min order amount value
 * @param status the status value
 * @param issuedAt the issued at value
 */
public record MyCouponResponse(
    Long issueId,
    Long couponId,
    String couponName,
    int discountAmount,
    int minOrderAmount,
    String status,
    LocalDateTime issuedAt
) {

    /**
     * Creates a response from the given domain object.
     * @param couponIssue the coupon issue value
     * @return the from result
     */
    public static MyCouponResponse from(CouponIssue couponIssue) {
        return new MyCouponResponse(
            couponIssue.getId(),
            couponIssue.getCoupon().getId(),
            couponIssue.getCoupon().getName(),
            couponIssue.getCoupon().getDiscountAmount(),
            couponIssue.getCoupon().getMinOrderAmount(),
            couponIssue.getStatus().name(),
            couponIssue.getIssuedAt()
        );
    }
}
