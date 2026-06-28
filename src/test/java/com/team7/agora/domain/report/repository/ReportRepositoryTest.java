package com.team7.agora.domain.report.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@Import(QuerydslConfig.class)
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

    private Report persistUserReport(String suffix) {
        User reporter = persistUser("reporter-" + suffix);
        User reportedUser = persistUser("reported-" + suffix);
        Report report = Report.user(reporter, reportedUser, "욕설 신고 " + suffix);
        entityManager.persist(report);
        return report;
    }

    private Report persistProductReport(String suffix) {
        User reporter = persistUser("product-reporter-" + suffix);
        User seller = persistUser("seller-" + suffix);
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
        Report report = Report.product(reporter, seller, product, "가품 신고 " + suffix);
        entityManager.persist(report);
        return report;
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
            "1168010100" + Math.abs(suffix.hashCode() % 100),
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
