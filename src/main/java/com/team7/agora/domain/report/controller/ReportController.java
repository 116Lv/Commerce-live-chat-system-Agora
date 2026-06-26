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
 * REST controller that exposes report endpoints.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * Creates a report controller instance.
     * @param reportService the report service value
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Creates product report data.
     * @param userDetails the user details value
     * @param request the request value
     * @return the create product report result
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
     * Creates user report data.
     * @param userDetails the user details value
     * @param request the request value
     * @return the create user report result
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
