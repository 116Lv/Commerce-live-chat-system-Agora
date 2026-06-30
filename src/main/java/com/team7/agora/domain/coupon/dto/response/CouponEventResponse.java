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
    String status,
    int remainingQuantity,
    double issueRate,
    boolean canIssue,
    boolean ended,
    boolean soldOut,
    String statusLabel,
    String typeLabel
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
            couponEvent.getStatus().name(),
            CouponResponseDisplay.remainingQuantity(couponEvent),
            CouponResponseDisplay.issueRate(couponEvent),
            CouponResponseDisplay.canIssue(couponEvent),
            CouponResponseDisplay.ended(couponEvent),
            CouponResponseDisplay.soldOut(couponEvent),
            CouponResponseDisplay.eventStatusLabel(couponEvent),
            CouponResponseDisplay.eventTypeLabel(couponEvent.getType())
        );
    }
}
