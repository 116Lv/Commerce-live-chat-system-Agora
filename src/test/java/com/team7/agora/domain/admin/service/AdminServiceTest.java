// 관리자 정보와 대시보드 서비스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.enums.ReportStatus;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TradeRepository tradeRepository;

    private AdminService createService() {
        return new AdminService(adminRepository, userRepository, reportRepository, productRepository, tradeRepository);
    }

    private AdminPrincipal principal(AdminRole role) {
        return new AdminPrincipal(1L, "admin@test.com", "encoded", role, AdminStatus.ACTIVE, "관리자");
    }

    @Test
    void getMe_returnsAdminAccountInfo() {
        // given
        AdminService service = createService();
        Admin admin = Admin.create("admin@test.com", "encoded", "관리자", AdminRole.ROOT_ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        // when
        AdminMeResponse response = service.getMe(principal(AdminRole.ROOT_ADMIN));

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("admin@test.com");
        assertThat(response.role()).isEqualTo("ROOT_ADMIN");
    }

    @Test
    void getDashboard_returnsMenusScopedToUserAdmin() {
        // given
        AdminService service = createService();

        // when
        AdminDashboardResponse response = service.getDashboard(principal(AdminRole.USER_ADMIN));

        // then
        assertThat(response.role()).isEqualTo("USER_ADMIN");
        assertThat(response.accessibleMenus()).containsExactly("USERS", "USER_REPORTS", "PRODUCT_REPORTS");
    }

    @Test
    void getDashboard_returnsMenusFromGrantedPermissions() {
        AdminService service = createService();
        AdminPrincipal admin = new AdminPrincipal(
                1L,
                "admin@test.com",
                "encoded",
                AdminRole.SETTLEMENT_ADMIN,
                AdminStatus.ACTIVE,
                "관리자",
                Set.of(AdminPermission.PAYMENT_MANAGE, AdminPermission.COUPON_MANAGE, AdminPermission.REPORT_MANAGE)
        );

        AdminDashboardResponse response = service.getDashboard(admin);

        assertThat(response.accessibleMenus())
                .contains("PAYMENTS", "REFUNDS", "SETTLEMENTS", "COUPONS", "USER_REPORTS", "PRODUCT_REPORTS");
    }

    @Test
    void getDashboard_returnsOperationalMetricsAndPendingReports() {
        AdminService service = createService();
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Report report = org.mockito.Mockito.mock(Report.class);

        when(userRepository.count()).thenReturn(1245L);
        when(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)).thenReturn(32L);
        when(reportRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)).thenReturn(8L);
        when(tradeRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)).thenReturn(156L);
        when(productRepository.countByApprovalStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                ProductApprovalStatus.PENDING,
                start,
                end
        )).thenReturn(5L);
        when(productRepository.countByApprovalStatus(ProductApprovalStatus.APPROVED)).thenReturn(87L);
        when(reportRepository.findTop5ByStatusOrderByCreatedAtAsc(ReportStatus.PENDING)).thenReturn(List.of(report));
        when(report.getId()).thenReturn(101L);
        when(report.getProduct()).thenReturn(null);
        when(report.getReportedUser()).thenReturn(null);
        when(report.getReason()).thenReturn("욕설");
        when(report.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 6, 29, 13, 30));
        when(report.getStatus()).thenReturn(ReportStatus.PENDING);

        AdminDashboardResponse response = service.getDashboard(principal(AdminRole.ROOT_ADMIN));

        assertThat(response.totalUserCount()).isEqualTo(1245L);
        assertThat(response.todayNewUserCount()).isEqualTo(32L);
        assertThat(response.todayReportCount()).isEqualTo(8L);
        assertThat(response.todayTradeCount()).isEqualTo(156L);
        assertThat(response.todayProductRequestCount()).isEqualTo(5L);
        assertThat(response.registeredProductCount()).isEqualTo(87L);
        assertThat(response.pendingReports()).hasSize(1);
        assertThat(response.pendingReports().get(0).reportId()).isEqualTo(101L);
        assertThat(response.pendingReports().get(0).target()).isEqualTo("-");
        assertThat(response.pendingReports().get(0).reason()).isEqualTo("욕설");
        assertThat(response.pendingReports().get(0).status()).isEqualTo("PENDING");
    }

    @Test
    void getDashboard_throwsUnauthorizedWithoutPrincipal() {
        // given
        AdminService service = createService();

        // when & then
        assertThatThrownBy(() -> service.getDashboard(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
