package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.report.entity.Report;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param reportId 입력 값
 * @param reporterId 입력 값
 * @param reportedUserId 입력 값
 * @param productId 입력 값
 * @param reason 입력 값
 * @param status 입력 값
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
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param report 입력 값
     * @return 처리 결과
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
