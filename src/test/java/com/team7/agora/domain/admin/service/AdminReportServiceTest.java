package com.team7.agora.domain.admin.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.admin.dto.response.AdminReportResponse;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    private AdminReportService adminReportService;

    private final CustomUserDetails userAdmin = new CustomUserDetails(
        99L, "user-admin@test.com", "pw", UserRole.USER_ADMIN, UserStatus.ACTIVE, "유저관리자"
    );
    private final CustomUserDetails regularUser = new CustomUserDetails(
        1L, "user@test.com", "pw", UserRole.ROLE_USER, UserStatus.ACTIVE, "일반유저"
    );

    private Report userReport;

    @BeforeEach
    void setUp() {
        adminReportService = new AdminReportService(reportRepository);
        User reporter = User.signup("reporter@test.com", "password", "신고자", "01011112222");
        assignId(reporter, 1L);
        User reportedUser = User.signup("reported@test.com", "password", "피신고자", "01044445555");
        assignId(reportedUser, 3L);
        userReport = Report.user(reporter, reportedUser, "욕설을 했습니다.");
        assignId(userReport, 200L);
    }

    @Test
    void getUserReports_returnsUserReportList() {
        when(reportRepository.findAllByProductIsNull()).thenReturn(List.of(userReport));

        List<AdminReportListResponse> responses = adminReportService.getUserReports(userAdmin);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).reportId()).isEqualTo(200L);
        assertThat(responses.get(0).status()).isEqualTo("PENDING");
    }

    @Test
    void getUserReports_rejectsNonUserAdmin() {
        assertThatThrownBy(() -> adminReportService.getUserReports(regularUser))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolveUserReport_resolvesReportAndBlocksUser() {
        when(reportRepository.findById(200L)).thenReturn(Optional.of(userReport));

        AdminReportResponse response = adminReportService.resolveUserReport(userAdmin, 200L, "욕설 확인");

        assertThat(response.reportId()).isEqualTo(200L);
        assertThat(response.status()).isEqualTo("RESOLVED");
        assertThat(userReport.getReportedUser().getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void resolveUserReport_rejectsNonUserAdmin() {
        assertThatThrownBy(() -> adminReportService.resolveUserReport(regularUser, 200L, "욕설 확인"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolveUserReport_rejectsProductReport() {
        assertThatThrownBy(() -> adminReportService.resolveUserReport(regularUser, 100L, "가품 판매 확인"))
            .isInstanceOf(BusinessException.class);
    }
}
