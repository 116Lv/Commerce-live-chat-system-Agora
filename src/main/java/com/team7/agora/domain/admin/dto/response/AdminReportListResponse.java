package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.report.entity.Report;

public record AdminReportListResponse(
    Long reportId,
    Long reporterId,
    Long reportedUserId,
    Long productId,
    String reason,
    String status
) {

    public static AdminReportListResponse from(Report report) {
        Long productId = report.getProduct() == null ? null : report.getProduct().getId();
        return new AdminReportListResponse(
            report.getId(),
            report.getReporter().getId(),
            report.getReportedUser().getId(),
            productId,
            report.getReason(),
            report.getStatus().name()
        );
    }
}
