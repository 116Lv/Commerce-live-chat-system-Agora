package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.enums.ReportStatus;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "agora.redisson.enabled=false",
    "agora.chat.redis-listener.enabled=false",
    "spring.sql.init.mode=never",
    "spring.jpa.show-sql=false"
})
class AdminReportServiceConcurrencyIntegrationTest {

    private final AdminReportService adminReportService;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Autowired
    AdminReportServiceConcurrencyIntegrationTest(
        AdminReportService adminReportService,
        ReportRepository reportRepository,
        UserRepository userRepository
    ) {
        this.adminReportService = adminReportService;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Test
    void concurrentResolveUserReportAllowsOnlyOneSuccessAndOneConflict() throws Exception {
        Report report = saveUserReport();
        AdminPrincipal admin = new AdminPrincipal(
            999L, "user-admin@test.com", "pw", AdminRole.USER_ADMIN, AdminStatus.ACTIVE, "유저관리자"
        );
        int requestCount = 2;
        var executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<ErrorCode>> futures = List.of(
                submitResolveRequest(executor, ready, start, admin, report.getId(), "첫 번째 처리"),
                submitResolveRequest(executor, ready, start, admin, report.getId(), "두 번째 처리")
            );

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<ErrorCode> results = futures.stream()
                .map(this::awaitResult)
                .toList();

            assertThat(results.stream().filter(Objects::isNull).count()).isEqualTo(1);
            assertThat(results.stream().filter(Objects::nonNull).toList()).containsExactly(ErrorCode.CONFLICT);
            assertThat(reportRepository.findById(report.getId()).orElseThrow().getStatus())
                .isEqualTo(ReportStatus.RESOLVED);
        } finally {
            executor.shutdownNow();
        }
    }

    private Future<ErrorCode> submitResolveRequest(
        ExecutorService executor,
        CountDownLatch ready,
        CountDownLatch start,
        AdminPrincipal admin,
        Long reportId,
        String adminMemo
    ) {
        return executor.submit(() -> {
            ready.countDown();
            start.await();
            try {
                adminReportService.resolveUserReport(admin, reportId, adminMemo);
                return null;
            } catch (BusinessException e) {
                return e.getErrorCode();
            }
        });
    }

    private ErrorCode awaitResult(Future<ErrorCode> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError("동시 신고 처리 결과를 시간 안에 받지 못했습니다.", e);
        }
    }

    private Report saveUserReport() {
        long suffix = System.nanoTime();
        User reporter = userRepository.save(User.signup(
            "reporter-" + suffix + "@test.com",
            "password",
            "신고자" + suffix,
            "010" + String.format("%08d", suffix % 100_000_000)
        ));
        User reportedUser = userRepository.save(User.signup(
            "reported-" + suffix + "@test.com",
            "password",
            "피신고자" + suffix,
            "011" + String.format("%08d", suffix % 100_000_000)
        ));
        return reportRepository.saveAndFlush(Report.user(reporter, reportedUser, "욕설을 했습니다."));
    }
}
