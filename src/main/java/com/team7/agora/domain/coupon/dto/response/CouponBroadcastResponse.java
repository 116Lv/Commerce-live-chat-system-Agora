package com.team7.agora.domain.coupon.dto.response;

/**
 * Response payload for returning coupon broadcast data.
 * @param couponId the coupon id value
 * @param issuedCount the issued count value
 * @param skippedCount the skipped count value
 */
public record CouponBroadcastResponse(
    Long couponId,
    int issuedCount,
    int skippedCount
) {
}
