package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;

    public AdminReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<AdminReportListResponse> getUserReports(CustomUserDetails admin) {
        validateUserAdmin(admin);
        return reportRepository.findAllByProductIsNull().stream()
            .map(AdminReportListResponse::from)
            .toList();
    }

    private void validateUserAdmin(CustomUserDetails admin) {
        if (admin == null || !AdminRoleSupport.isUserAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "유저 신고 내역 조회는 관리자만 수행할 수 있습니다.");
        }
    }
}
