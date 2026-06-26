// 관리자 로그인과 로그아웃 API를 제공하는 컨트롤러
package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminLoginRequest;
import com.team7.agora.domain.admin.dto.response.AdminLoginResponse;
import com.team7.agora.domain.admin.service.AdminAuthService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes admin auth endpoints.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * Creates a admin auth controller instance.
     * @param adminAuthService the admin auth service value
     */
    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * Handles login behavior.
     * @param request the request value
     * @return the login result
     */
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success("관리자 로그인이 완료되었습니다.", adminAuthService.login(request));
    }

    /**
     * Handles logout behavior.
     * @param admin the admin value
     * @return the logout result
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails admin) {
        adminAuthService.logout(admin);
        return ApiResponse.success("관리자 로그아웃이 완료되었습니다.", null);
    }
}
