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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param adminUserService 입력 값
     */
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @param page 입력 값
     * @param size 입력 값
     * @return 처리 결과
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
     * 요청한 동작을 처리한다.
     * @param admin 입력 값
     * @param userId 입력 값
     * @param request 입력 값
     * @return 처리 결과
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
