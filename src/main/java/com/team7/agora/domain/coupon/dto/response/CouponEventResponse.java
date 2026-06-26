package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;

public record CouponEventResponse(
    Long eventId,
    String type,
    String name,
    int totalQuantity,
    int issuedQuantity,
    int discountAmount,
    int minOrderAmount,
    int validDays,
    LocalDateTime startAt,
    LocalDateTime endAt,
    String status
) {

    public static CouponEventResponse from(CouponEvent couponEvent) {
        return new CouponEventResponse(
            couponEvent.getId(),
            couponEvent.getType().name(),
            couponEvent.getName(),
            couponEvent.getTotalQuantity(),
            couponEvent.getIssuedQuantity(),
            couponEvent.getDiscountAmount(),
            couponEvent.getMinOrderAmount(),
            couponEvent.getValidDays(),
            couponEvent.getStartAt(),
            couponEvent.getEndAt(),
            couponEvent.getStatus().name()
        );
    }
}
