package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminSettlementResponse;
import com.team7.agora.domain.admin.service.AdminSettlementService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes admin settlement endpoints.
 */
@RestController
@RequestMapping("/api/admin/settlements")
public class AdminSettlementController {

    private final AdminSettlementService adminSettlementService;

    /**
     * Creates a admin settlement controller instance.
     * @param adminSettlementService the admin settlement service value
     */
    public AdminSettlementController(AdminSettlementService adminSettlementService) {
        this.adminSettlementService = adminSettlementService;
    }

    /**
     * Handles settle behavior.
     * @param userDetails the user details value
     * @param settlementId the settlement id value
     * @return the settle result
     */
    @PostMapping("/{settlementId}/settle")
    public ApiResponse<AdminSettlementResponse> settle(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long settlementId
    ) {
        AdminSettlementResponse response = adminSettlementService.settle(userDetails, settlementId);
        return ApiResponse.success("정산을 실행했습니다.", response);
    }
}
