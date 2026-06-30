package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;

public record AdminCouponEventResponse(
    Long eventId,
    String type,
    String name,
    LocalDateTime startAt,
    LocalDateTime endAt,
    int totalQuantity,
    int issuedQuantity,
    int discountAmount,
    int minOrderAmount,
    int validDays,
    String status,
    int remainingQuantity,
    double issueRate,
    boolean canIssue,
    boolean ended,
    boolean soldOut,
    String statusLabel,
    String typeLabel
) {

    public static AdminCouponEventResponse from(CouponEvent event) {
        return new AdminCouponEventResponse(
            event.getId(),
            event.getType().name(),
            event.getName(),
            event.getStartAt(),
            event.getEndAt(),
            event.getTotalQuantity(),
            event.getIssuedQuantity(),
            event.getDiscountAmount(),
            event.getMinOrderAmount(),
            event.getValidDays(),
            event.getStatus().name(),
            CouponResponseDisplay.remainingQuantity(event),
            CouponResponseDisplay.issueRate(event),
            CouponResponseDisplay.canIssue(event),
            CouponResponseDisplay.ended(event),
            CouponResponseDisplay.soldOut(event),
            CouponResponseDisplay.eventStatusLabel(event),
            CouponResponseDisplay.eventTypeLabel(event.getType())
        );
    }
}
