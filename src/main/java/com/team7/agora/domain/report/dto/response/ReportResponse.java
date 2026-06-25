package com.team7.agora.domain.report.dto.response;

import com.team7.agora.domain.report.entity.Report;
import java.time.LocalDateTime;

public record ReportResponse(
    Long reportId,
    Long reporterId,
    Long reportedUserId,
    Long productId,
    String reason,
    String status,
    LocalDateTime createdAt
) {

    public static ReportResponse from(Report report) {
        Long productId = report.getProduct() == null ? null : report.getProduct().getId();
        return new ReportResponse(
            report.getId(),
            report.getReporter().getId(),
            report.getReportedUser().getId(),
            productId,
            report.getReason(),
            report.getStatus().name(),
            report.getCreatedAt()
        );
    }
}
