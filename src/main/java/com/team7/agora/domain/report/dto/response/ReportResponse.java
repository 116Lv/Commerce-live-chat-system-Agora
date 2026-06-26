package com.team7.agora.domain.report.dto.response;

import com.team7.agora.domain.report.entity.Report;
import java.time.LocalDateTime;

/**
 * 신고 응답 본문을 표현하는 DTO이다.
 * @param reportId 신고 ID
 * @param reporterId 신고를 등록한 회원 ID
 * @param reportedUserId 신고 대상 회원 ID
 * @param productId 상품 ID
 * @param reason 처리 사유
 * @param status 조회 또는 변경할 상태
 * @param createdAt 데이터가 생성된 시각
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
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param report 응답으로 변환할 신고 엔티티
     * @return 클라이언트에 반환할 API 응답
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
