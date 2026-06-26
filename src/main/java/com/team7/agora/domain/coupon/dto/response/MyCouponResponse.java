package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponIssue;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param issueId 입력 값
 * @param couponId 입력 값
 * @param couponName 입력 값
 * @param discountAmount 입력 값
 * @param minOrderAmount 입력 값
 * @param status 입력 값
 * @param issuedAt 입력 값
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
     * @param couponIssue 입력 값
     * @return 처리 결과
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
