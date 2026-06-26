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

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param adminReportService 입력 값
     */
    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @return 처리 결과
     */
    @GetMapping("/users")
    public ApiResponse<List<AdminReportListResponse>> getUserReports(
        @AuthenticationPrincipal CustomUserDetails admin
    ) {
        return ApiResponse.success("유저 신고 내역을 조회했습니다.", adminReportService.getUserReports(admin));
    }

    /**
     * 요청한 동작을 처리한다.
     * @param admin 입력 값
     * @param reportId 입력 값
     * @param request 입력 값
     * @return 처리 결과
     */
    @PostMapping("/users/{reportId}/resolve")
    public ApiResponse<AdminReportResponse> resolveUserReport(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long reportId,
        @Valid @RequestBody AdminReportResolveRequest request
    ) {
        AdminReportResponse response = adminReportService.resolveUserReport(admin, reportId, request.adminMemo());
        return ApiResponse.success("유저 신고를 처리했습니다.", response);
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @return 처리 결과
     */
    @GetMapping("/products")
    public ApiResponse<List<AdminReportListResponse>> getProductReports(
        @AuthenticationPrincipal CustomUserDetails admin
    ) {
        return ApiResponse.success("상품 신고 내역을 조회했습니다.", adminReportService.getProductReports(admin));
    }

    /**
     * 요청한 동작을 처리한다.
     * @param admin 입력 값
     * @param reportId 입력 값
     * @param request 입력 값
     * @return 처리 결과
     */
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
