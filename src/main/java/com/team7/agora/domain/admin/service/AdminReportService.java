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
 * 관리자 신고 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param reportRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public AdminReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * 'getUserReports' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public List<AdminReportListResponse> getUserReports(CustomUserDetails admin) {
        validateUserAdmin(admin);
        return reportRepository.findAllByProductIsNull().stream()
            .map(AdminReportListResponse::from)
            .toList();
    }

    /**
     * 'resolveUserReport' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param admin 인증된 관리자 정보
     * @param reportId 신고 ID
     * @param adminMemo 관리자가 신고 처리 시 남기는 메모
     * @return 클라이언트에 반환할 API 응답
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
     * 'getProductReports' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public List<AdminReportListResponse> getProductReports(CustomUserDetails admin) {
        validateProductAdmin(admin);
        return reportRepository.findAllByProductIsNotNull().stream()
            .map(AdminReportListResponse::from)
            .toList();
    }

    /**
     * 'resolveProductReport' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param admin 인증된 관리자 정보
     * @param reportId 신고 ID
     * @param adminMemo 관리자가 신고 처리 시 남기는 메모
     * @return 클라이언트에 반환할 API 응답
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
