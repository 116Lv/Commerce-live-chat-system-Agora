package com.team7.agora.domain.coupon.dto.response;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param couponId 입력 값
 * @param issuedCount 입력 값
 * @param skippedCount 입력 값
 */
public record CouponBroadcastResponse(
    Long couponId,
    int issuedCount,
    int skippedCount
) {
}
