package com.team7.agora.domain.report.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.enums.ReportStatus;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.test.support.RepositorySliceTest;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@RepositorySliceTest
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findAllByProductIsNull_fetchesReporterAndReportedUserForAdminList() {
        Report firstReport = persistUserReport("first");
        Report secondReport = persistUserReport("second");
        flushAndClear();

        List<Report> reports = reportRepository.findAllByProductIsNull();

        assertThat(reports).extracting(Report::getId)
            .containsExactlyInAnyOrder(firstReport.getId(), secondReport.getId());
        assertThat(reports)
            .allSatisfy(report -> {
                assertThat(Hibernate.isInitialized(report.getReporter())).isTrue();
                assertThat(Hibernate.isInitialized(report.getReportedUser())).isTrue();
                assertThat(report.getProduct()).isNull();
            });
    }

    @Test
    void findAllByProductIsNotNull_fetchesReporterReportedUserAndProductForAdminList() {
        Report firstReport = persistProductReport("first");
        Report secondReport = persistProductReport("second");
        flushAndClear();

        List<Report> reports = reportRepository.findAllByProductIsNotNull();

        assertThat(reports).extracting(Report::getId)
            .containsExactlyInAnyOrder(firstReport.getId(), secondReport.getId());
        assertThat(reports)
            .allSatisfy(report -> {
                assertThat(Hibernate.isInitialized(report.getReporter())).isTrue();
                assertThat(Hibernate.isInitialized(report.getReportedUser())).isTrue();
                assertThat(Hibernate.isInitialized(report.getProduct())).isTrue();
            });
    }

    @Test
    void existsByReporterAndReportedUserAndProductIsNullAndStatus_findsPendingUserDuplicate() {
        User reporter = persistUser("pending-user-reporter");
        User reportedUser = persistUser("pending-user-reported");
        persistUserReport(reporter, reportedUser, ReportStatus.PENDING, "pending-user");
        flushAndClear();

        assertThat(reportRepository.existsByReporterAndReportedUserAndProductIsNullAndStatus(
            reporter,
            reportedUser,
            ReportStatus.PENDING
        )).isTrue();
    }

    @Test
    void existsByReporterAndReportedUserAndProductIsNullAndStatus_ignoresResolvedAndRejectedUserReports() {
        for (ReportStatus status : List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED)) {
            String suffix = status == ReportStatus.RESOLVED ? "ur-rs" : "ur-rj";
            User reporter = persistUser("reporter-" + suffix);
            User reportedUser = persistUser("reported-" + suffix);
            persistUserReport(reporter, reportedUser, status, suffix);
            flushAndClear();

            assertThat(reportRepository.existsByReporterAndReportedUserAndProductIsNullAndStatus(
                reporter,
                reportedUser,
                status
            )).as(status.name() + " test setup").isTrue();
            assertThat(reportRepository.existsByReporterAndReportedUserAndProductIsNullAndStatus(
                reporter,
                reportedUser,
                ReportStatus.PENDING
            )).as(status.name() + " report should not block a new report").isFalse();
        }
    }

    @Test
    void existsByReporterAndProductAndStatus_findsPendingProductDuplicate() {
        User reporter = persistUser("pending-product-reporter");
        User seller = persistUser("pending-product-seller");
        Product product = persistProduct(seller, "pending-product");
        persistProductReport(reporter, seller, product, ReportStatus.PENDING, "pending-product");
        flushAndClear();

        assertThat(reportRepository.existsByReporterAndProductAndStatus(
            reporter,
            product,
            ReportStatus.PENDING
        )).isTrue();
    }

    @Test
    void existsByReporterAndProductAndStatus_ignoresResolvedAndRejectedProductReports() {
        for (ReportStatus status : List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED)) {
            String suffix = status == ReportStatus.RESOLVED ? "pr-rs" : "pr-rj";
            User reporter = persistUser("reporter-" + suffix);
            User seller = persistUser("seller-" + suffix);
            Product product = persistProduct(seller, suffix);
            persistProductReport(reporter, seller, product, status, suffix);
            flushAndClear();

            assertThat(reportRepository.existsByReporterAndProductAndStatus(
                reporter,
                product,
                status
            )).as(status.name() + " test setup").isTrue();
            assertThat(reportRepository.existsByReporterAndProductAndStatus(
                reporter,
                product,
                ReportStatus.PENDING
            )).as(status.name() + " report should not block a new report").isFalse();
        }
    }

    private Report persistUserReport(String suffix) {
        User reporter = persistUser("reporter-" + suffix);
        User reportedUser = persistUser("reported-" + suffix);
        return persistUserReport(reporter, reportedUser, ReportStatus.PENDING, suffix);
    }

    private Report persistUserReport(User reporter, User reportedUser, ReportStatus status, String suffix) {
        Report report = Report.user(reporter, reportedUser, "욕설 신고 " + suffix);
        applyStatus(report, status);
        entityManager.persist(report);
        return report;
    }

    private Report persistProductReport(String suffix) {
        User reporter = persistUser("product-reporter-" + suffix);
        User seller = persistUser("seller-" + suffix);
        Product product = persistProduct(seller, suffix);
        return persistProductReport(reporter, seller, product, ReportStatus.PENDING, suffix);
    }

    private Report persistProductReport(
        User reporter,
        User seller,
        Product product,
        ReportStatus status,
        String suffix
    ) {
        Report report = Report.product(reporter, seller, product, "가품 신고 " + suffix);
        applyStatus(report, status);
        entityManager.persist(report);
        return report;
    }

    private Product persistProduct(User seller, String suffix) {
        Region region = persistRegion(suffix);
        Product product = Product.create(
            seller,
            region,
            "신고 상품 " + suffix,
            "상품 설명 " + suffix,
            BigDecimal.valueOf(50000),
            "디지털"
        );
        entityManager.persist(product);
        return product;
    }

    private void applyStatus(Report report, ReportStatus status) {
        if (status == ReportStatus.RESOLVED) {
            report.resolve("처리 완료");
            return;
        }
        if (status == ReportStatus.REJECTED) {
            ReflectionTestUtils.setField(report, "status", status);
        }
    }

    private User persistUser(String suffix) {
        User user = User.signup(
            suffix + "@test.com",
            "password",
            "회원-" + suffix,
            "010" + Math.abs(suffix.hashCode() % 10_000_000)
        );
        entityManager.persist(user);
        return user;
    }

    private Region persistRegion(String suffix) {
        Region region = Region.create(
            "서울 강남구 " + suffix,
            "1168010100" + Math.abs(suffix.hashCode() % 1_000_000),
            "서울",
            "강남구",
            suffix
        );
        entityManager.persist(region);
        return region;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
