package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.Coupon;
import java.time.LocalDateTime;

public record CouponEventCouponResponse(
    Long couponId,
    Long userId,
    String userNickname,
    String userEmail,
    String eventName,
    int discountAmount,
    int minOrderAmount,
    String status,
    String statusLabel,
    LocalDateTime issuedAt,
    LocalDateTime expiresAt
) {

    public static CouponEventCouponResponse from(Coupon coupon) {
        var event = coupon.getCouponEvent();
        return new CouponEventCouponResponse(
            coupon.getId(),
            coupon.getUser() == null ? null : coupon.getUser().getId(),
            coupon.getUser() == null ? null : coupon.getUser().getNickname(),
            coupon.getUser() == null ? null : coupon.getUser().getEmail(),
            event.getName(),
            event.getDiscountAmount(),
            event.getMinOrderAmount(),
            coupon.getStatus().name(),
            CouponResponseDisplay.couponStatusLabel(coupon.getStatus(), coupon.getExpiresAt()),
            coupon.getIssuedAt(),
            coupon.getExpiresAt()
        );
    }
}
