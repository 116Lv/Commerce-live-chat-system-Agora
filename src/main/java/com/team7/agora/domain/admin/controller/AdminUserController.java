package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminUserStatusUpdateRequest;
import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.service.AdminUserService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import com.team7.agora.global.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes admin user endpoints.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Creates a admin user controller instance.
     * @param adminUserService the admin user service value
     */
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Returns users data.
     * @param admin the admin value
     * @param page the page value
     * @param size the size value
     * @return the get users result
     */
    @GetMapping
    public ApiResponse<PageResponse<AdminUserResponse>> getUsers(
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminUserResponse> responses = adminUserService.getUsers(admin, PageRequest.of(page, size));
        return ApiResponse.success("사용자 목록을 조회했습니다.", PageResponse.from(responses));
    }

    /**
     * Handles change status behavior.
     * @param admin the admin value
     * @param userId the user id value
     * @param request the request value
     * @return the change status result
     */
    @PatchMapping("/{userId}/status")
    public ApiResponse<AdminUserResponse> changeStatus(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        AdminUserResponse response = adminUserService.changeStatus(admin, userId, request.status());
        return ApiResponse.success("사용자 상태가 변경되었습니다.", response);
    }
}
