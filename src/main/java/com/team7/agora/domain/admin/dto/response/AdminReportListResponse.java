package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.user.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Admin Report List 응답 본문을 표현하는 DTO이다.
 * @param reportId 신고 ID
 * @param reporterId 신고를 등록한 회원 ID
 * @param reportedUserId 신고 대상 회원 ID
 * @param productId 상품 ID
 * @param reason 처리 사유
 * @param status 조회 또는 변경할 상태
 */
public record AdminReportListResponse(
    Long reportId,
    Long reporterId,
    String reporterNickname,
    String reporterEmail,
    Long reportedUserId,
    String reportedUserNickname,
    String reportedUserEmail,
    Long productId,
    String productTitle,
    BigDecimal productPrice,
    String productStatus,
    String productApprovalStatus,
    Long productSellerId,
    String productSellerNickname,
    String productSellerEmail,
    String reason,
    String status,
    String adminMemo,
    LocalDateTime resolvedAt
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param report 응답으로 변환할 신고 엔티티
     * @return 클라이언트에 반환할 API 응답
     */
    public static AdminReportListResponse from(Report report) {
        Product product = report.getProduct();
        User seller = product == null ? null : product.getSeller();
        Long productId = product == null ? null : product.getId();
        String productTitle = product == null ? null : product.getTitle();
        return new AdminReportListResponse(
            report.getId(),
            report.getReporter().getId(),
            report.getReporter().getNickname(),
            report.getReporter().getEmail(),
            report.getReportedUser().getId(),
            report.getReportedUser().getNickname(),
            report.getReportedUser().getEmail(),
            productId,
            productTitle,
            product == null ? null : product.getPrice(),
            product == null ? null : product.getStatus().name(),
            product == null ? null : product.getApprovalStatus().name(),
            seller == null ? null : seller.getId(),
            seller == null ? null : seller.getNickname(),
            seller == null ? null : seller.getEmail(),
            report.getReason(),
            report.getStatus().name(),
            report.getAdminMemo(),
            report.getResolvedAt()
        );
    }
}
