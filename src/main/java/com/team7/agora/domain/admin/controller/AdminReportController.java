package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminReportResolveRequest;
import com.team7.agora.domain.admin.dto.response.AdminReportListResponse;
import com.team7.agora.domain.admin.dto.response.AdminReportResponse;
import com.team7.agora.domain.admin.service.AdminReportService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 신고 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminReportService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    /**
     * 관리자 신고 정보를 조회하는 GET /api/admin/reports/users 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('ROOT_ADMIN', 'USER_ADMIN')")
    public ApiResponse<List<AdminReportListResponse>> getUserReports(
        @AuthenticationPrincipal CustomUserDetails admin
    ) {
        return ApiResponse.success("유저 신고 내역을 조회했습니다.", adminReportService.getUserReports(admin));
    }

    /**
     * 관리자 신고 상태를 변경하는 POST /api/admin/reports/users/{reportId}/resolve 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @param reportId 대상 신고 ID
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/users/{reportId}/resolve")
    @PreAuthorize("hasAnyAuthority('ROOT_ADMIN', 'USER_ADMIN')")
    public ApiResponse<AdminReportResponse> resolveUserReport(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long reportId,
        @Valid @RequestBody AdminReportResolveRequest request
    ) {
        AdminReportResponse response = adminReportService.resolveUserReport(admin, reportId, request.adminMemo());
        return ApiResponse.success("유저 신고를 처리했습니다.", response);
    }

    /**
     * 관리자 신고 정보를 조회하는 GET /api/admin/reports/products 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/products")
    @PreAuthorize("hasAnyAuthority('ROOT_ADMIN', 'PRODUCT_ADMIN')")
    public ApiResponse<List<AdminReportListResponse>> getProductReports(
        @AuthenticationPrincipal CustomUserDetails admin
    ) {
        return ApiResponse.success("상품 신고 내역을 조회했습니다.", adminReportService.getProductReports(admin));
    }

    /**
     * 관리자 신고 상태를 변경하는 POST /api/admin/reports/products/{reportId}/resolve 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @param reportId 대상 신고 ID
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/products/{reportId}/resolve")
    @PreAuthorize("hasAnyAuthority('ROOT_ADMIN', 'PRODUCT_ADMIN')")
    public ApiResponse<AdminReportResponse> resolveProductReport(
        @AuthenticationPrincipal CustomUserDetails admin,
        @PathVariable Long reportId,
        @Valid @RequestBody AdminReportResolveRequest request
    ) {
        AdminReportResponse response = adminReportService.resolveProductReport(admin, reportId, request.adminMemo());
        return ApiResponse.success("상품 신고를 처리했습니다.", response);
    }
}
