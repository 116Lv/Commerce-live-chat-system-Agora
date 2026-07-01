package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload;

public record AdminCouponIssueApprovalResponse(
    Long approvalRequestId,
    String status,
    Long eventId,
    int inputCount,
    int validTargetCount,
    int duplicateCount,
    int excludedCount,
    int plannedIssueCount,
    int expectedIssuedQuantity,
    boolean exceedsRemainingQuantity
) {

    public static AdminCouponIssueApprovalResponse from(AdminCouponApprovalPayload payload) {
        return new AdminCouponIssueApprovalResponse(
            payload.getApprovalRequest().getId(),
            payload.getApprovalRequest().getStatus().name(),
            payload.getCouponEventId(),
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
