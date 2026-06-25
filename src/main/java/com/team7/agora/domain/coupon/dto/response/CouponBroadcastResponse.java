package com.team7.agora.domain.coupon.dto.response;

public record CouponBroadcastResponse(
    Long couponId,
    int issuedCount,
    int skippedCount
) {
}
