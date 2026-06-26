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
 * 신고 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param reportService 신고 비즈니스 로직을 처리하는 서비스
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 신고 정보를 생성하거나 준비하는 POST /api/reports/products 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
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
     * 신고 정보를 생성하거나 준비하는 POST /api/reports/users 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
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
