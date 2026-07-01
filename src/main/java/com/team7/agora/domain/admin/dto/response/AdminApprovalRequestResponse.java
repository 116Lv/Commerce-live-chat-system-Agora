package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.coupon.dto.response.AdminCouponApprovalPayloadResponse;
import com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload;
import java.time.LocalDateTime;

public record AdminApprovalRequestResponse(
        Long id,
        String operation,
        String status,
        Long requesterId,
        String requesterEmail,
        String requesterNickname,
        Long targetAdminId,
        String targetAdminEmail,
        String targetAdminNickname,
        String requestedRole,
        String reason,
        Long approverId,
        String approverNickname,
        String decisionMemo,
        LocalDateTime decidedAt,
        LocalDateTime createdAt,
        AdminCouponApprovalPayloadResponse couponPayload
) {

    public static AdminApprovalRequestResponse from(AdminApprovalRequest request) {
        return from(request, null);
    }

    public static AdminApprovalRequestResponse from(
            AdminApprovalRequest request,
            AdminCouponApprovalPayload couponPayload
    ) {
        Admin requester = request.getRequester();
        Admin targetAdmin = request.getTargetAdmin();
        Admin approver = request.getApprover();

        return new AdminApprovalRequestResponse(
                request.getId(),
                request.getOperation().name(),
                request.getStatus().name(),
                requester.getId(),
                requester.getEmail(),
                requester.getNickname(),
                targetAdmin == null ? null : targetAdmin.getId(),
                targetAdmin == null ? null : targetAdmin.getEmail(),
                targetAdmin == null ? null : targetAdmin.getNickname(),
                request.getRequestedRole() == null ? null : request.getRequestedRole().name(),
                request.getReason(),
                approver == null ? null : approver.getId(),
                approver == null ? null : approver.getNickname(),
                request.getDecisionMemo(),
                request.getDecidedAt(),
                request.getCreatedAt(),
                AdminCouponApprovalPayloadResponse.from(couponPayload)
        );
    }

    public AdminApprovalRequestResponse(
            Long id,
            String operation,
            String status,
            Long requesterId,
            String requesterEmail,
            String requesterNickname,
            Long targetAdminId,
            String targetAdminEmail,
            String targetAdminNickname,
            String requestedRole,
            String reason,
            Long approverId,
            String approverNickname,
            String decisionMemo,
            LocalDateTime decidedAt,
            LocalDateTime createdAt
    ) {
        this(
                id,
                operation,
                status,
                requesterId,
                requesterEmail,
                requesterNickname,
                targetAdminId,
                targetAdminEmail,
                targetAdminNickname,
                requestedRole,
                reason,
                approverId,
                approverNickname,
                decisionMemo,
                decidedAt,
                createdAt,
                null
        );
    }
}
