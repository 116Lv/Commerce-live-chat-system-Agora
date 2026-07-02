package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.admin.dto.response.AdminReportResponse;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.global.auth.AdminPrincipal;
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
    public List<AdminReportListResponse> getUserReports(AdminPrincipal admin) {
        return getUserReports(admin, null, null);
    }

    public List<AdminReportListResponse> getUserReports(AdminPrincipal admin, String status, String search) {
        validateUserAdmin(admin);
        return reportRepository.findAllByProductIsNull().stream()
            .filter(report -> matchesStatus(report, status))
            .filter(report -> matchesSearch(report, search))
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
    public AdminReportResponse resolveUserReport(AdminPrincipal admin, Long reportId, String adminMemo) {
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
    public List<AdminReportListResponse> getProductReports(AdminPrincipal admin) {
        return getProductReports(admin, null, null);
    }

    public List<AdminReportListResponse> getProductReports(AdminPrincipal admin, String status, String search) {
        validateProductAdmin(admin);
        return reportRepository.findAllByProductIsNotNull().stream()
            .filter(report -> matchesStatus(report, status))
            .filter(report -> matchesSearch(report, search))
            .map(AdminReportListResponse::from)
            .toList();
    }

    private boolean matchesStatus(Report report, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return report.getStatus().name().equalsIgnoreCase(status.trim());
    }

    private boolean matchesSearch(Report report, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String keyword = search.trim().toLowerCase();
        return contains(report.getId(), keyword)
            || contains(report.getReporter().getId(), keyword)
            || contains(report.getReportedUser().getId(), keyword)
            || (report.getProduct() != null && contains(report.getProduct().getId(), keyword))
            || contains(report.getStatus().name(), keyword)
            || contains(report.getReason(), keyword)
            || contains(report.getReporter().getEmail(), keyword)
            || contains(report.getReporter().getNickname(), keyword)
            || contains(report.getReportedUser().getEmail(), keyword)
            || contains(report.getReportedUser().getNickname(), keyword)
            || (report.getProduct() != null && contains(report.getProduct().getTitle(), keyword));
    }

    private boolean contains(Long value, String keyword) {
        return value != null && String.valueOf(value).contains(keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    /**
     * 'resolveProductReport' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param admin 인증된 관리자 정보
     * @param reportId 신고 ID
     * @param adminMemo 관리자가 신고 처리 시 남기는 메모
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public AdminReportResponse resolveProductReport(AdminPrincipal admin, Long reportId, String adminMemo) {
        validateProductAdmin(admin);
        Report report = findReport(reportId);
        if (report.getProduct() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상품 신고가 아닙니다.");
        }

        report.getProduct().hide();
        report.resolve(adminMemo);
        return AdminReportResponse.from(report);
    }

    private Report findReport(Long reportId) {
        return reportRepository.findByIdForUpdate(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));
    }

    private void validateUserAdmin(AdminPrincipal admin) {
        if (!AdminRoleSupport.hasPermission(admin, AdminPermission.REPORT_MANAGE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "유저 신고 처리는 관리자만 수행할 수 있습니다.");
        }
    }

    private void validateProductAdmin(AdminPrincipal admin) {
        if (!AdminRoleSupport.hasPermission(admin, AdminPermission.REPORT_MANAGE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "상품 신고 처리는 관리자만 수행할 수 있습니다.");
        }
    }
}
