package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.report.entity.Report;
import java.time.LocalDateTime;

/**
 * 관리자 신고 응답 본문을 표현하는 DTO이다.
 * @param reportId 신고 ID
 * @param reportedUserId 신고 대상 회원 ID
 * @param status 조회 또는 변경할 상태
 * @param adminMemo 관리자가 신고 처리 시 남기는 메모
 * @param resolvedAt 신고 처리가 완료된 시각
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
     * @param report 응답으로 변환할 신고 엔티티
     * @return 클라이언트에 반환할 API 응답
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
