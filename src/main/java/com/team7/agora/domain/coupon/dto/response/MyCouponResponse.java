package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponIssue;
import java.time.LocalDateTime;

public record MyCouponResponse(
    Long issueId,
    Long couponId,
    String couponName,
    int discountAmount,
    int minOrderAmount,
    String status,
    LocalDateTime issuedAt
) {

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
