package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
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
        LocalDateTime createdAt
) {

    public static AdminApprovalRequestResponse from(AdminApprovalRequest request) {
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
                targetAdmin.getId(),
                targetAdmin.getEmail(),
                targetAdmin.getNickname(),
                request.getRequestedRole().name(),
                request.getReason(),
                approver == null ? null : approver.getId(),
                approver == null ? null : approver.getNickname(),
                request.getDecisionMemo(),
                request.getDecidedAt(),
                request.getCreatedAt()
        );
    }
}
