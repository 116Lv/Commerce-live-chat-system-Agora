package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.report.entity.Report;

/**
 * Response payload for returning admin report list data.
 * @param reportId the report id value
 * @param reporterId the reporter id value
 * @param reportedUserId the reported user id value
 * @param productId the product id value
 * @param reason the reason value
 * @param status the status value
 */
public record AdminReportListResponse(
    Long reportId,
    Long reporterId,
    Long reportedUserId,
    Long productId,
    String reason,
    String status
) {

    /**
     * Creates a response from the given domain object.
     * @param report the report value
     * @return the from result
     */
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
