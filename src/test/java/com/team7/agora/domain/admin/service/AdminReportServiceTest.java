package com.team7.agora.domain.admin.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.admin.dto.response.AdminReportResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
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

    @Mock
    private ProductSearchService productSearchService;

    private AdminReportService adminReportService;

    private final AdminPrincipal userAdmin = new AdminPrincipal(
        99L, "user-admin@test.com", "pw", AdminRole.USER_ADMIN, AdminStatus.ACTIVE, "유저관리자"
    );
    private final AdminPrincipal productAdmin = new AdminPrincipal(
        98L, "product-admin@test.com", "pw", AdminRole.PRODUCT_ADMIN, AdminStatus.ACTIVE, "상품관리자"
    );
    private final AdminPrincipal settlementAdmin = new AdminPrincipal(
        1L, "settlement-admin@test.com", "pw", AdminRole.SETTLEMENT_ADMIN, AdminStatus.ACTIVE, "정산관리자"
    );

    private Report userReport;
    private Report productReport;

    @BeforeEach
    void setUp() {
        adminReportService = new AdminReportService(reportRepository, productSearchService);
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
        assertThat(responses.get(0).reporterEmail()).isEqualTo("reporter@test.com");
        assertThat(responses.get(0).reportedUserEmail()).isEqualTo("reported@test.com");
    }

    @Test
    void getUserReports_filtersByStatusAndSearchText() {
        userReport.resolve("already handled");
        when(reportRepository.findAllByProductIsNull()).thenReturn(List.of(userReport));

        assertThat(adminReportService.getUserReports(userAdmin, "PENDING", "reported")).isEmpty();
        assertThat(adminReportService.getUserReports(userAdmin, "RESOLVED", "reported")).hasSize(1);
        assertThat(adminReportService.getUserReports(userAdmin, null, "200")).hasSize(1);
        assertThat(adminReportService.getUserReports(userAdmin, null, "resolved")).hasSize(1);
        assertThat(adminReportService.getUserReports(userAdmin, null, "3")).hasSize(1);
    }

    @Test
    void getUserReports_rejectsNonUserAdmin() {
        assertThatThrownBy(() -> adminReportService.getUserReports(settlementAdmin))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolveUserReport_resolvesReportAndBlocksUser() {
        when(reportRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(userReport));

        AdminReportResponse response = adminReportService.resolveUserReport(userAdmin, 200L, "욕설 확인");

        assertThat(response.reportId()).isEqualTo(200L);
        assertThat(response.status()).isEqualTo("RESOLVED");
        assertThat(userReport.getReportedUser().getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void resolveUserReport_locksReportBeforeResolving() {
        when(reportRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(userReport));

        adminReportService.resolveUserReport(userAdmin, 200L, "욕설 확인");

        verify(reportRepository).findByIdForUpdate(200L);
    }

    @Test
    void resolveUserReport_rejectsNonUserAdmin() {
        assertThatThrownBy(() -> adminReportService.resolveUserReport(settlementAdmin, 200L, "욕설 확인"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolveUserReport_rejectsProductReport() {
        when(reportRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(productReport));

        assertThatThrownBy(() -> adminReportService.resolveUserReport(userAdmin, 100L, "가품 판매 확인"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolveUserReport_rejectsAlreadyResolvedReport() {
        userReport.resolve("이미 처리됨");
        when(reportRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(userReport));

        assertThatThrownBy(() -> adminReportService.resolveUserReport(userAdmin, 200L, "다시 처리"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void getProductReports_returnsProductReportList() {
        when(reportRepository.findAllByProductIsNotNull()).thenReturn(List.of(productReport));

        List<AdminReportListResponse> responses = adminReportService.getProductReports(productAdmin);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).reportId()).isEqualTo(100L);
        assertThat(responses.get(0).productId()).isEqualTo(10L);
        assertThat(responses.get(0).status()).isEqualTo("PENDING");
        assertThat(responses.get(0).productPrice()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(responses.get(0).productStatus()).isEqualTo("SELLING");
        assertThat(responses.get(0).productApprovalStatus()).isEqualTo("PENDING");
        assertThat(responses.get(0).productSellerId()).isEqualTo(2L);
        assertThat(responses.get(0).productSellerEmail()).isEqualTo("seller@test.com");
        assertThat(responses.get(0).adminMemo()).isNull();
        assertThat(responses.get(0).resolvedAt()).isNull();
        assertThat(responses.get(0).productTitle()).isEqualTo("가품 의심 상품");
    }

    @Test
    void getProductReports_includesResolvedMemo() {
        productReport.resolve("신고 사유 확인");
        when(reportRepository.findAllByProductIsNotNull()).thenReturn(List.of(productReport));

        List<AdminReportListResponse> responses = adminReportService.getProductReports(productAdmin, "RESOLVED", null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).adminMemo()).isEqualTo("신고 사유 확인");
        assertThat(responses.get(0).resolvedAt()).isNotNull();
    }

    @Test
    void getProductReports_filtersSearchByReportProductReporterAndStatusFields() {
        when(reportRepository.findAllByProductIsNotNull()).thenReturn(List.of(productReport));

        assertThat(adminReportService.getProductReports(productAdmin, null, "100")).hasSize(1);
        assertThat(adminReportService.getProductReports(productAdmin, null, "10")).hasSize(1);
        assertThat(adminReportService.getProductReports(productAdmin, null, "1")).hasSize(1);
        assertThat(adminReportService.getProductReports(productAdmin, null, "pending")).hasSize(1);
        assertThat(adminReportService.getProductReports(productAdmin, null, "nothing")).isEmpty();
    }

    @Test
    void resolveProductReport_rejectsAlreadyResolvedReport() {
        productReport.resolve("이미 처리됨");
        when(reportRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(productReport));

        assertThatThrownBy(() -> adminReportService.resolveProductReport(productAdmin, 100L, "다시 처리"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void getProductReports_rejectsNonProductAdmin() {
        assertThatThrownBy(() -> adminReportService.getProductReports(settlementAdmin))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolveProductReport_resolvesReport() {
        when(reportRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(productReport));

        AdminReportResponse response = adminReportService.resolveProductReport(productAdmin, 100L, "가품 판매 확인");

        assertThat(response.reportId()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo("RESOLVED");
        assertThat(productReport.getProduct().getStatus().name()).isEqualTo("HIDDEN");
        assertThat(productReport.getProduct().getApprovalStatus().name()).isEqualTo("REJECTED");
        assertThat(response.adminMemo()).isEqualTo("가품 판매 확인");
        verify(productSearchService).evictSearchCache();
    }

    @Test
    void resolveProductReport_locksReportBeforeResolving() {
        when(reportRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(productReport));

        adminReportService.resolveProductReport(productAdmin, 100L, "가품 판매 확인");

        verify(reportRepository).findByIdForUpdate(100L);
    }

    @Test
    void resolveProductReport_rejectsNonProductAdmin() {
        assertThatThrownBy(() -> adminReportService.resolveProductReport(settlementAdmin, 100L, "가품 판매 확인"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolveProductReport_rejectsUserReport() {
        when(reportRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(userReport));

        assertThatThrownBy(() -> adminReportService.resolveProductReport(productAdmin, 200L, "욕설 확인"))
            .isInstanceOf(BusinessException.class);
    }
}
