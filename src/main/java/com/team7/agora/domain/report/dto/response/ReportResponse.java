package com.team7.agora.domain.report.dto.response;

import com.team7.agora.domain.report.entity.Report;
import java.time.LocalDateTime;

/**
 * Response payload for returning report data.
 * @param reportId the report id value
 * @param reporterId the reporter id value
 * @param reportedUserId the reported user id value
 * @param productId the product id value
 * @param reason the reason value
 * @param status the status value
 * @param createdAt the created at value
 */
public record ReportResponse(
    Long reportId,
    Long reporterId,
    Long reportedUserId,
    Long productId,
    String reason,
    String status,
    LocalDateTime createdAt
) {

    /**
     * Creates a response from the given domain object.
     * @param report the report value
     * @return the from result
     */
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
