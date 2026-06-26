package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.admin.dto.response.AdminReportResponse;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that coordinates admin report use cases.
 */
@Service
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;

    /**
     * Creates a admin report service instance.
     * @param reportRepository the report repository value
     */
    public AdminReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Returns user reports data.
     * @param admin the admin value
     * @return the get user reports result
     */
    public List<AdminReportListResponse> getUserReports(CustomUserDetails admin) {
        validateUserAdmin(admin);
        return reportRepository.findAllByProductIsNull().stream()
            .map(AdminReportListResponse::from)
            .toList();
    }

    /**
     * Handles resolve user report behavior.
     * @param admin the admin value
     * @param reportId the report id value
     * @param adminMemo the admin memo value
     * @return the resolve user report result
     */
    @Transactional
    public AdminReportResponse resolveUserReport(CustomUserDetails admin, Long reportId, String adminMemo) {
        validateUserAdmin(admin);
        Report report = findReport(reportId);
        if (report.getProduct() != null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "유저 신고가 아닙니다.");
        }

        report.getReportedUser().block();
        report.resolve(adminMemo);
        return AdminReportResponse.from(report);
    }

    /**
     * Returns product reports data.
     * @param admin the admin value
     * @return the get product reports result
     */
    public List<AdminReportListResponse> getProductReports(CustomUserDetails admin) {
        validateProductAdmin(admin);
        return reportRepository.findAllByProductIsNotNull().stream()
            .map(AdminReportListResponse::from)
            .toList();
    }

    /**
     * Handles resolve product report behavior.
     * @param admin the admin value
     * @param reportId the report id value
     * @param adminMemo the admin memo value
     * @return the resolve product report result
     */
    @Transactional
    public AdminReportResponse resolveProductReport(CustomUserDetails admin, Long reportId, String adminMemo) {
        validateProductAdmin(admin);
        Report report = findReport(reportId);
        if (report.getProduct() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상품 신고가 아닙니다.");
        }

        report.resolve(adminMemo);
        return AdminReportResponse.from(report);
    }

    private Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));
    }

    private void validateUserAdmin(CustomUserDetails admin) {
        if (admin == null || !AdminRoleSupport.isUserAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "유저 신고 처리는 관리자만 수행할 수 있습니다.");
        }
    }

    private void validateProductAdmin(CustomUserDetails admin) {
        if (admin == null || !AdminRoleSupport.isProductAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "상품 신고 처리는 관리자만 수행할 수 있습니다.");
        }
    }
}
