package com.team7.agora.domain.report.controller;

import com.team7.agora.domain.report.dto.request.ProductReportCreateRequest;
import com.team7.agora.domain.report.dto.request.UserReportCreateRequest;
import com.team7.agora.domain.report.dto.response.ReportResponse;
import com.team7.agora.domain.report.service.ReportService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param reportService 입력 값
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 도메인 객체를 생성한다.
     * @param userDetails 입력 값
     * @param request 입력 값
     * @return 처리 결과
     */
    @PostMapping("/products")
    public ApiResponse<ReportResponse> createProductReport(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductReportCreateRequest request
    ) {
        ReportResponse response = reportService.createProductReport(
            userDetails.getUserId(),
            request.productId(),
            request.reason()
        );
        return ApiResponse.success("신고가 접수되었습니다.", response);
    }

    /**
     * 도메인 객체를 생성한다.
     * @param userDetails 입력 값
     * @param request 입력 값
     * @return 처리 결과
     */
    @PostMapping("/users")
    public ApiResponse<ReportResponse> createUserReport(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody UserReportCreateRequest request
    ) {
        ReportResponse response = reportService.createUserReport(
            userDetails.getUserId(),
            request.reportedUserId(),
            request.reason()
        );
        return ApiResponse.success("신고가 접수되었습니다.", response);
    }
}
