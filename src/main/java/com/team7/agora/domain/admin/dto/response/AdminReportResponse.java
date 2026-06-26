package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.report.entity.Report;
import java.time.LocalDateTime;

/**
 * Response payload for returning admin report data.
 * @param reportId the report id value
 * @param reportedUserId the reported user id value
 * @param status the status value
 * @param adminMemo the admin memo value
 * @param resolvedAt the resolved at value
 */
public record AdminReportResponse(
    Long reportId,
    Long reportedUserId,
    String status,
    String adminMemo,
    LocalDateTime resolvedAt
) {

    /**
     * Creates a response from the given domain object.
     * @param report the report value
     * @return the from result
     */
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
