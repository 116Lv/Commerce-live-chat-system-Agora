package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.request.AdminApprovalDecisionRequest;
import com.team7.agora.domain.admin.dto.request.AdminApprovalRoleChangeRequest;
import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.service.AdminApprovalService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminApprovalController {

    private final AdminApprovalService adminApprovalService;

    public AdminApprovalController(AdminApprovalService adminApprovalService) {
        this.adminApprovalService = adminApprovalService;
    }

    @GetMapping("/approval-requests")
    @PreAuthorize("hasAuthority('APPROVAL_MANAGE')")
    public ApiResponse<List<AdminApprovalRequestResponse>> getRequests(
            @AuthenticationPrincipal AdminPrincipal admin,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success("Approval requests have been loaded.", adminApprovalService.getRequests(admin, status));
    }

    @GetMapping("/approval-requests/{requestId}")
    @PreAuthorize("hasAuthority('APPROVAL_MANAGE')")
    public ApiResponse<AdminApprovalRequestResponse> getRequestDetail(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long requestId
    ) {
        return ApiResponse.success("Approval request has been loaded.", adminApprovalService.getRequestDetail(admin, requestId));
    }

    @PostMapping("/approval-requests/{requestId}/approve")
    @PreAuthorize("hasAuthority('APPROVAL_MANAGE')")
    public ApiResponse<AdminApprovalRequestResponse> approve(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long requestId,
            @Valid @RequestBody AdminApprovalDecisionRequest request
    ) {
        AdminApprovalRequestResponse response = adminApprovalService.approve(admin, requestId, request.memo());
        return ApiResponse.success("Approval request has been approved.", response);
    }

    @PostMapping("/approval-requests/{requestId}/reject")
    @PreAuthorize("hasAuthority('APPROVAL_MANAGE')")
    public ApiResponse<AdminApprovalRequestResponse> reject(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long requestId,
            @Valid @RequestBody AdminApprovalDecisionRequest request
    ) {
        AdminApprovalRequestResponse response = adminApprovalService.reject(admin, requestId, request.memo());
        return ApiResponse.success("Approval request has been rejected.", response);
    }

    @GetMapping("/my-approval-requests")
    public ApiResponse<List<AdminApprovalRequestResponse>> getMyRequests(
            @AuthenticationPrincipal AdminPrincipal admin
    ) {
        return ApiResponse.success("My approval requests have been loaded.", adminApprovalService.getMyRequests(admin));
    }

    @PostMapping("/my-approval-requests/role-change")
    public ApiResponse<AdminApprovalRequestResponse> requestRoleChange(
            @AuthenticationPrincipal AdminPrincipal admin,
            @Valid @RequestBody AdminApprovalRoleChangeRequest request
    ) {
        AdminApprovalRequestResponse response = adminApprovalService.requestRoleChange(
                admin,
                request.targetAdminId(),
                request.requestedRole(),
                request.reason()
        );
        return ApiResponse.success("Approval request has been created.", response);
    }
}
