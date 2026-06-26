// 관리자 내 정보와 대시보드 API를 제공하는 컨트롤러
package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.admin.service.AdminService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes admin endpoints.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * Creates a admin controller instance.
     * @param adminService the admin service value
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Returns me data.
     * @param admin the admin value
     * @return the get me result
     */
    @GetMapping("/me")
    public ApiResponse<AdminMeResponse> getMe(@AuthenticationPrincipal CustomUserDetails admin) {
        return ApiResponse.success("관리자 정보를 조회했습니다.", adminService.getMe(admin));
    }

    /**
     * Returns dashboard data.
     * @param admin the admin value
     * @return the get dashboard result
     */
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard(@AuthenticationPrincipal CustomUserDetails admin) {
        return ApiResponse.success("대시보드를 조회했습니다.", adminService.getDashboard(admin));
    }
}
