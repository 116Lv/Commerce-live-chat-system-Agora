package com.team7.agora.domain.report.service;

import com.team7.agora.domain.report.dto.response.ReportResponse;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReportResponse createUserReport(Long reporterId, Long reportedUserId, String reason) {
        if (reporterId.equals(reportedUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "본인을 신고할 수 없습니다.");
        }

        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고자를 찾을 수 없습니다."));
        User reportedUser = userRepository.findById(reportedUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 회원을 찾을 수 없습니다."));

        Report report = Report.user(reporter, reportedUser, reason);
        return ReportResponse.from(reportRepository.save(report));
    }
}
