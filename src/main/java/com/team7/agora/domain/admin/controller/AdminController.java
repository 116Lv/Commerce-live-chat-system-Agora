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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param adminService 입력 값
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @return 처리 결과
     */
    @GetMapping("/me")
    public ApiResponse<AdminMeResponse> getMe(@AuthenticationPrincipal CustomUserDetails admin) {
        return ApiResponse.success("관리자 정보를 조회했습니다.", adminService.getMe(admin));
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @return 처리 결과
     */
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard(@AuthenticationPrincipal CustomUserDetails admin) {
        return ApiResponse.success("대시보드를 조회했습니다.", adminService.getDashboard(admin));
    }
}
