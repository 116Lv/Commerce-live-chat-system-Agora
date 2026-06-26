// ROOT_ADMIN의 관리자 권한 변경 API를 제공하는 컨트롤러
package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminAccountRoleUpdateRequest;
import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.service.AdminAccountService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes admin account endpoints.
 */
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    /**
     * Creates a admin account controller instance.
     * @param adminAccountService the admin account service value
     */
    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    /**
     * Handles change role behavior.
     * @param admin the admin value
     * @param userId the user id value
     * @param request the request value
     * @return the change role result
     */
    @PatchMapping("/{userId}/role")
    public ApiResponse<AdminUserResponse> changeRole(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long userId,
            @Valid @RequestBody AdminAccountRoleUpdateRequest request
    ) {
        AdminUserResponse response = adminAccountService.changeRole(admin, userId, request.role());
        return ApiResponse.success("관리자 권한이 변경되었습니다.", response);
    }
}
