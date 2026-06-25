package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminReportResolveRequest;
import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.admin.dto.response.AdminReportResponse;
import com.team7.agora.domain.admin.service.AdminReportService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminReportListResponse>> getUserReports(
        @AuthenticationPrincipal CustomUserDetails admin
    ) {
        return ApiResponse.success("유저 신고 내역을 조회했습니다.", adminReportService.getUserReports(admin));
    }

    @PostMapping("/users/{reportId}/resolve")
    public ApiResponse<AdminReportResponse> resolveUserReport(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long reportId,
        @Valid @RequestBody AdminReportResolveRequest request
    ) {
        AdminReportResponse response = adminReportService.resolveUserReport(admin, reportId, request.adminMemo());
        return ApiResponse.success("유저 신고를 처리했습니다.", response);
    }

    @GetMapping("/products")
    public ApiResponse<List<AdminReportListResponse>> getProductReports(
        @AuthenticationPrincipal CustomUserDetails admin
    ) {
        return ApiResponse.success("상품 신고 내역을 조회했습니다.", adminReportService.getProductReports(admin));
    }

    @PostMapping("/products/{reportId}/resolve")
    public ApiResponse<AdminReportResponse> resolveProductReport(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long reportId,
        @Valid @RequestBody AdminReportResolveRequest request
    ) {
        AdminReportResponse response = adminReportService.resolveProductReport(admin, reportId, request.adminMemo());
        return ApiResponse.success("상품 신고를 처리했습니다.", response);
    }
}
