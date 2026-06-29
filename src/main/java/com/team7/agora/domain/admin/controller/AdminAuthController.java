// 관리자 로그인과 로그아웃 API를 제공하는 컨트롤러
package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminLoginRequest;
import com.team7.agora.domain.admin.dto.response.AdminLoginResponse;
import com.team7.agora.domain.admin.service.AdminAuthService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 인증 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminAuthService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 관리자 인증 로그인을 처리하는 POST /api/admin/auth/login 요청을 처리한다.
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success("관리자 로그인이 완료되었습니다.", adminAuthService.login(request));
    }

    /**
     * 관리자 인증 로그아웃을 처리하는 POST /api/admin/auth/logout 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal AdminPrincipal admin) {
        adminAuthService.logout(admin);
        return ApiResponse.success("관리자 로그아웃이 완료되었습니다.", null);
    }
}
