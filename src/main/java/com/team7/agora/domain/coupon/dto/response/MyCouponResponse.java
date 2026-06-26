package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponIssue;
import java.time.LocalDateTime;

/**
 * 내 쿠폰 응답 본문을 표현하는 DTO이다.
 * @param issueId 쿠폰 발급 이력 ID
 * @param couponId 쿠폰 ID
 * @param couponName 쿠폰 이름
 * @param discountAmount 쿠폰 할인 금액
 * @param minOrderAmount 쿠폰 사용을 위한 최소 주문 금액
 * @param status 조회 또는 변경할 상태
 * @param issuedAt 쿠폰이 발급된 시각
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
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param couponIssue 쿠폰 발급 이력 엔티티
     * @return 클라이언트에 반환할 API 응답
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
