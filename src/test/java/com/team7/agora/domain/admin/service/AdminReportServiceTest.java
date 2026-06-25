package com.team7.agora.domain.admin.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.admin.dto.response.AdminReportResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import java.math.BigDecimal;
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
    private final CustomUserDetails productAdmin = new CustomUserDetails(
        98L, "product-admin@test.com", "pw", UserRole.PRODUCT_ADMIN, UserStatus.ACTIVE, "상품관리자"
    );
    private final CustomUserDetails regularUser = new CustomUserDetails(
        1L, "user@test.com", "pw", UserRole.ROLE_USER, UserStatus.ACTIVE, "일반유저"
    );

    private Report userReport;
    private Report productReport;

    @BeforeEach
    void setUp() {
        adminReportService = new AdminReportService(reportRepository);
        User reporter = User.signup("reporter@test.com", "password", "신고자", "01011112222");
        assignId(reporter, 1L);
        User reportedUser = User.signup("reported@test.com", "password", "피신고자", "01044445555");
        assignId(reportedUser, 3L);
        userReport = Report.user(reporter, reportedUser, "욕설을 했습니다.");
        assignId(userReport, 200L);

        User seller = User.signup("seller@test.com", "password", "판매자", "01033334444");
        assignId(seller, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        Product product = Product.create(seller, region, "가품 의심 상품", "설명이 이상해요", BigDecimal.valueOf(50000), "디지털");
        assignId(product, 10L);
        productReport = Report.product(reporter, seller, product, "가품이 의심됩니다.");
        assignId(productReport, 100L);
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
        when(reportRepository.findById(100L)).thenReturn(Optional.of(productReport));

        assertThatThrownBy(() -> adminReportService.resolveUserReport(userAdmin, 100L, "가품 판매 확인"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void getProductReports_returnsProductReportList() {
        when(reportRepository.findAllByProductIsNotNull()).thenReturn(List.of(productReport));

        List<AdminReportListResponse> responses = adminReportService.getProductReports(productAdmin);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).reportId()).isEqualTo(100L);
        assertThat(responses.get(0).productId()).isEqualTo(10L);
        assertThat(responses.get(0).status()).isEqualTo("PENDING");
    }

    @Test
    void getProductReports_rejectsNonProductAdmin() {
        assertThatThrownBy(() -> adminReportService.getProductReports(regularUser))
            .isInstanceOf(BusinessException.class);
    }
}
