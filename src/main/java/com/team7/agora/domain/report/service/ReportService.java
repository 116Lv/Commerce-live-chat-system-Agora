package com.team7.agora.domain.report.service;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.report.dto.response.ReportResponse;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신고 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param reportRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     * @param productRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public ReportService(
        ReportRepository reportRepository,
        UserRepository userRepository,
        ProductRepository productRepository
    ) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * 사용자가 상품에 대한 신고 내용을 등록한다.
     * @param reporterId 신고를 등록한 회원 ID
     * @param productId 상품 ID
     * @param reason 처리 사유
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ReportResponse createProductReport(Long reporterId, Long productId, String reason) {
        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고자를 찾을 수 없습니다."));
        Product product = productRepository.findByIdForUpdateAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (product.isSeller(reporterId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "내 상품은 신고할 수 없습니다.");
        }

        if (reportRepository.existsByReporterAndProduct(reporter, product)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 신고한 상품입니다.");
        }

        Report report = Report.product(reporter, product.getSeller(), product, reason);
        return ReportResponse.from(reportRepository.save(report));
    }

    /**
     * 사용자가 다른 회원에 대한 신고 내용을 등록한다.
     * @param reporterId 신고를 등록한 회원 ID
     * @param reportedUserId 신고 대상 회원 ID
     * @param reason 처리 사유
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ReportResponse createUserReport(Long reporterId, Long reportedUserId, String reason) {
        if (reporterId.equals(reportedUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "본인을 신고할 수 없습니다.");
        }

        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고자를 찾을 수 없습니다."));
        User reportedUser = userRepository.findByIdForUpdate(reportedUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 회원을 찾을 수 없습니다."));

        if (reportRepository.existsByReporterAndReportedUserAndProductIsNull(reporter, reportedUser)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 신고한 회원입니다.");
        }

        Report report = Report.user(reporter, reportedUser, reason);
        return ReportResponse.from(reportRepository.save(report));
    }
}
