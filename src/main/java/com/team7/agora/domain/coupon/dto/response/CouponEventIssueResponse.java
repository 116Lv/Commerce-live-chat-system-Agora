package com.team7.agora.domain.coupon.dto.response;

public record CouponEventIssueResponse(
    Long eventId,
    int issuedCount,
    int skippedCount
) {
    public static CouponEventIssueResponse issued(Long eventId) {
        return new CouponEventIssueResponse(eventId, 1, 0);
    }
}
