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
 * 관리자 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 관리자 정보를 조회하는 GET /api/admin/me 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/me")
    public ApiResponse<AdminMeResponse> getMe(@AuthenticationPrincipal CustomUserDetails admin) {
        return ApiResponse.success("관리자 정보를 조회했습니다.", adminService.getMe(admin));
    }

    /**
     * 관리자 정보를 조회하는 GET /api/admin/dashboard 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard(@AuthenticationPrincipal CustomUserDetails admin) {
        return ApiResponse.success("대시보드를 조회했습니다.", adminService.getDashboard(admin));
    }
}
