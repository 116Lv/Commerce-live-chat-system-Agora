package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.report.entity.Report;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param reportId 입력 값
 * @param reportedUserId 입력 값
 * @param status 입력 값
 * @param adminMemo 입력 값
 * @param resolvedAt 입력 값
 */
public record AdminReportResponse(
    Long reportId,
    Long reportedUserId,
    String status,
    String adminMemo,
    LocalDateTime resolvedAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param report 입력 값
     * @return 처리 결과
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
