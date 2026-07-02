package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminUserStatusUpdateRequest;
import com.team7.agora.domain.admin.dto.response.AdminManagedUserResponse;
import com.team7.agora.domain.admin.service.AdminUserService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.response.ApiResponse;
import com.team7.agora.global.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 회원 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('USER_MANAGE')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminUserService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 관리자 회원 정보를 조회하는 GET /api/admin/users 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 항목 개수
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping
    public ApiResponse<PageResponse<AdminManagedUserResponse>> getUsers(
            @AuthenticationPrincipal AdminPrincipal admin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminManagedUserResponse> responses = adminUserService.getUsers(admin, PageRequest.of(page, size));
        return ApiResponse.success("사용자 목록을 조회했습니다.", PageResponse.from(responses));
    }

    /**
     * 관리자 회원 상태를 변경하는 PATCH /api/admin/users/{userId}/status 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @param userId 대상 회원 ID
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @PatchMapping("/{userId}/status")
    public ApiResponse<AdminManagedUserResponse> changeStatus(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        AdminManagedUserResponse response = adminUserService.changeStatus(admin, userId, request.status());
        return ApiResponse.success("사용자 상태가 변경되었습니다.", response);
    }
}
