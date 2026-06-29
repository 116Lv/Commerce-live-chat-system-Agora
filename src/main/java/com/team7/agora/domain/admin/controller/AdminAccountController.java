// ROOT_ADMIN의 관리자 권한 변경 API를 제공하는 컨트롤러
package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminAccountRoleUpdateRequest;
import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.service.AdminAccountService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 계정 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminAccountService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    /**
     * 관리자 계정 상태를 변경하는 PATCH /api/admin/accounts/{userId}/role 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @param userId 대상 회원 ID
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PatchMapping("/{userId}/role")
    public ApiResponse<AdminUserResponse> changeRole(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long userId,
            @Valid @RequestBody AdminAccountRoleUpdateRequest request
    ) {
        AdminUserResponse response = adminAccountService.changeRole(admin, userId, request.role());
        return ApiResponse.success("관리자 권한이 변경되었습니다.", response);
    }
}
