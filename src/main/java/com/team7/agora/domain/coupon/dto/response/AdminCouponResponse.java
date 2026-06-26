package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.Coupon;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param couponId 입력 값
 * @param name 입력 값
 * @param discountAmount 입력 값
 * @param minOrderAmount 입력 값
 * @param type 입력 값
 * @param status 입력 값
 * @param validDays 입력 값
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
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param coupon 입력 값
     * @return 처리 결과
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
