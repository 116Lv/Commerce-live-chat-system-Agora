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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param adminAuthService 입력 값
     */
    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param request 입력 값
     * @return 처리 결과
     */
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success("관리자 로그인이 완료되었습니다.", adminAuthService.login(request));
    }

    /**
     * 요청한 동작을 처리한다.
     * @param admin 입력 값
     * @return 처리 결과
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails admin) {
        adminAuthService.logout(admin);
        return ApiResponse.success("관리자 로그아웃이 완료되었습니다.", null);
    }
}
