package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.Coupon;
import java.time.LocalDateTime;

public record CouponEventCouponResponse(
    Long couponId,
    Long userId,
    String userNickname,
    String status,
    LocalDateTime issuedAt,
    LocalDateTime expiresAt
) {

    public static CouponEventCouponResponse from(Coupon coupon) {
        return new CouponEventCouponResponse(
            coupon.getId(),
            coupon.getUser() == null ? null : coupon.getUser().getId(),
            coupon.getUser() == null ? null : coupon.getUser().getNickname(),
            coupon.getStatus().name(),
            coupon.getIssuedAt(),
            coupon.getExpiresAt()
        );
    }
}
