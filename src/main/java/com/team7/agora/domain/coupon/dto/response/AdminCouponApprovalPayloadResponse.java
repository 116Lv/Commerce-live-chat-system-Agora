package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload;
import java.time.LocalDateTime;
import java.util.List;

public record AdminCouponApprovalPayloadResponse(
        Long couponEventId,
        String eventType,
        String eventName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer totalQuantity,
        Integer discountAmount,
        Integer minOrderAmount,
        Integer validDays,
        List<Long> targetUserIds,
        int inputCount,
        int validTargetCount,
        int duplicateCount,
        int excludedCount,
        int plannedIssueCount,
        int expectedIssuedQuantity,
        boolean exceedsRemainingQuantity
) {

    public static AdminCouponApprovalPayloadResponse from(AdminCouponApprovalPayload payload) {
        if (payload == null) {
            return null;
        }
        return new AdminCouponApprovalPayloadResponse(
                payload.getCouponEventId(),
                payload.getEventType() == null ? null : payload.getEventType().name(),
                payload.getEventName(),
                payload.getStartAt(),
                payload.getEndAt(),
                payload.getTotalQuantity(),
                payload.getDiscountAmount(),
                payload.getMinOrderAmount(),
                payload.getValidDays(),
                payload.targetUserIdList(),
                payload.getInputCount(),
                payload.getValidTargetCount(),
                payload.getDuplicateCount(),
                payload.getExcludedCount(),
                payload.getPlannedIssueCount(),
                payload.getExpectedIssuedQuantity(),
                payload.isExceedsRemainingQuantity()
        );
    }
}
