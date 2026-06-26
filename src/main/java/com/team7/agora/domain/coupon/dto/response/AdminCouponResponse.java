package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.Coupon;

/**
 * Admin Coupon 응답 본문을 표현하는 DTO이다.
 * @param couponId 쿠폰 ID
 * @param name 이름 또는 제목
 * @param discountAmount 쿠폰 할인 금액
 * @param minOrderAmount 쿠폰 사용을 위한 최소 주문 금액
 * @param type 쿠폰 유형
 * @param status 조회 또는 변경할 상태
 * @param validDays 쿠폰 유효 일수
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
     * @param coupon 쿠폰 엔티티
     * @return 클라이언트에 반환할 API 응답
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
