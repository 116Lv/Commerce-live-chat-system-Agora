package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.report.entity.Report;
import java.time.LocalDateTime;

public record AdminReportResponse(
    Long reportId,
    Long reportedUserId,
    String status,
    String adminMemo,
    LocalDateTime resolvedAt
) {

    public static AdminReportResponse from(Report report) {
        return new AdminReportResponse(
            report.getId(),
            report.getReportedUser().getId(),
            report.getStatus().name(),
            report.getAdminMemo(),
            report.getResolvedAt()
        );
    }
}
