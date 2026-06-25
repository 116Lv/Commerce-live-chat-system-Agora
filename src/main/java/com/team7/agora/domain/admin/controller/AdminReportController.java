package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.admin.service.AdminReportService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
}
