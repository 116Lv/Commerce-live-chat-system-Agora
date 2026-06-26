package com.team7.agora.domain.coupon.dto.response;

/**
 * Coupon Broadcast 응답 본문을 표현하는 DTO이다.
 * @param couponId 쿠폰 ID
 * @param issuedCount 발급 성공 건수
 * @param skippedCount 중복 또는 조건 미충족으로 건너뛴 건수
 */
public record CouponBroadcastResponse(
    Long couponId,
    int issuedCount,
    int skippedCount
) {
}
