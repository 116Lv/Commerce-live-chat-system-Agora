package com.team7.agora.domain.report.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.report.dto.response.ReportResponse;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.enums.ReportStatus;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    private ReportService reportService;
    private User reporter;
    private User reportedUser;
    private User seller;
    private Product product;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository, userRepository, productRepository);
        reporter = User.signup("reporter@test.com", "password", "신고자", "01011112222");
        assignId(reporter, 1L);
        reportedUser = User.signup("reported@test.com", "password", "피신고자", "01044445555");
        assignId(reportedUser, 3L);
        seller = User.signup("seller@test.com", "password", "판매자", "01033334444");
        assignId(seller, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        product = Product.create(seller, region, "가품 의심 상품", "설명이 이상해요", BigDecimal.valueOf(50000), "디지털");
        assignId(product, 10L);
    }

    @Test
    void createUserReport_storesPendingReport() {
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(reporter));
        when(userRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(reportedUser));
        when(reportRepository.existsByReporterAndReportedUserAndProductIsNullAndStatus(
            reporter,
            reportedUser,
            ReportStatus.PENDING
        )).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            assignId(report, 200L);
            return report;
        });

        ReportResponse response = reportService.createUserReport(1L, 3L, "욕설을 했습니다.");

        assertThat(response.reportId()).isEqualTo(200L);
        assertThat(response.reporterId()).isEqualTo(1L);
        assertThat(response.reportedUserId()).isEqualTo(3L);
        assertThat(response.productId()).isNull();
        assertThat(response.status()).isEqualTo("PENDING");
        verify(reportRepository).existsByReporterAndReportedUserAndProductIsNullAndStatus(
            reporter,
            reportedUser,
            ReportStatus.PENDING
        );
    }

    @Test
    void createUserReport_rejectsSelfReport() {
        assertThatThrownBy(() -> reportService.createUserReport(1L, 1L, "욕설을 했습니다."))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void createProductReport_storesPendingReport() {
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(reporter));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(reportRepository.existsByReporterAndProductAndStatus(reporter, product, ReportStatus.PENDING))
            .thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            assignId(report, 100L);
            return report;
        });

        ReportResponse response = reportService.createProductReport(1L, 10L, "가품이 의심됩니다.");

        assertThat(response.reportId()).isEqualTo(100L);
        assertThat(response.reporterId()).isEqualTo(1L);
        assertThat(response.reportedUserId()).isEqualTo(2L);
        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo("PENDING");
        verify(reportRepository).existsByReporterAndProductAndStatus(reporter, product, ReportStatus.PENDING);
    }

    @Test
    void createProductReport_rejectsOwnProduct() {
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> reportService.createProductReport(2L, 10L, "내 상품 신고 시도"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void createUserReport_rejectsDuplicateReport() {
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(reporter));
        when(userRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(reportedUser));
        when(reportRepository.existsByReporterAndReportedUserAndProductIsNullAndStatus(
            reporter,
            reportedUser,
            ReportStatus.PENDING
        )).thenReturn(true);

        assertThatThrownBy(() -> reportService.createUserReport(1L, 3L, "중복 신고"))
            .isInstanceOf(BusinessException.class);
        verify(reportRepository).existsByReporterAndReportedUserAndProductIsNullAndStatus(
            reporter,
            reportedUser,
            ReportStatus.PENDING
        );
    }

    @Test
    void createProductReport_rejectsDuplicateReport() {
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(reporter));
        when(productRepository.findByIdForUpdateAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(reportRepository.existsByReporterAndProductAndStatus(reporter, product, ReportStatus.PENDING))
            .thenReturn(true);

        assertThatThrownBy(() -> reportService.createProductReport(1L, 10L, "중복 신고"))
            .isInstanceOf(BusinessException.class);
        verify(reportRepository).existsByReporterAndProductAndStatus(reporter, product, ReportStatus.PENDING);
    }

    @Test
    void createProductReport_rejectsNonActiveReporter() {
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.createProductReport(1L, 10L, "비활성 신고"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void createUserReport_rejectsNonActiveReporter() {
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.createUserReport(1L, 3L, "비활성 신고"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
