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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param adminAccountService 입력 값
     */
    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param admin 입력 값
     * @param userId 입력 값
     * @param request 입력 값
     * @return 처리 결과
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
