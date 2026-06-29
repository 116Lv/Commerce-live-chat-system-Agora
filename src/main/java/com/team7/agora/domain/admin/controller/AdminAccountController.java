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

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @PatchMapping("/{adminId}/role")
    public ApiResponse<AdminUserResponse> changeRole(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long adminId,
            @Valid @RequestBody AdminAccountRoleUpdateRequest request
    ) {
        AdminUserResponse response = adminAccountService.changeRole(admin, adminId, request.role());
        return ApiResponse.success("Admin role has been updated.", response);
    }
}
