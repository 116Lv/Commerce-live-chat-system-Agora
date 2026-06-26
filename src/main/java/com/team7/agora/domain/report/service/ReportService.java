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
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param reportRepository 입력 값
     * @param userRepository 입력 값
     * @param productRepository 입력 값
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
     * 도메인 객체를 생성한다.
     * @param reporterId 입력 값
     * @param productId 입력 값
     * @param reason 입력 값
     * @return 처리 결과
     */
    @Transactional
    public ReportResponse createProductReport(Long reporterId, Long productId, String reason) {
        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고자를 찾을 수 없습니다."));
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (product.isSeller(reporterId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "내 상품은 신고할 수 없습니다.");
        }

        Report report = Report.product(reporter, product.getSeller(), product, reason);
        return ReportResponse.from(reportRepository.save(report));
    }

    /**
     * 도메인 객체를 생성한다.
     * @param reporterId 입력 값
     * @param reportedUserId 입력 값
     * @param reason 입력 값
     * @return 처리 결과
     */
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
