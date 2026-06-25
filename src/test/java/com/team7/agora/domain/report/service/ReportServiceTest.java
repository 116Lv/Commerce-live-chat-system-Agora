package com.team7.agora.domain.report.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.report.dto.response.ReportResponse;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
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

    private ReportService reportService;
    private User reporter;
    private User reportedUser;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository, userRepository);
        reporter = User.signup("reporter@test.com", "password", "신고자", "01011112222");
        assignId(reporter, 1L);
        reportedUser = User.signup("reported@test.com", "password", "피신고자", "01044445555");
        assignId(reportedUser, 3L);
    }

    @Test
    void createUserReport_storesPendingReport() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(3L)).thenReturn(Optional.of(reportedUser));
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
    }

    @Test
    void createUserReport_rejectsSelfReport() {
        assertThatThrownBy(() -> reportService.createUserReport(1L, 1L, "욕설을 했습니다."))
            .isInstanceOf(BusinessException.class);
    }
}
