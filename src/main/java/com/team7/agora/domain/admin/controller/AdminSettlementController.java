package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminSettlementResponse;
import com.team7.agora.domain.admin.service.AdminSettlementService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 정산 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/settlements")
@PreAuthorize("hasAuthority('PAYMENT_MANAGE')")
public class AdminSettlementController {

    private final AdminSettlementService adminSettlementService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminSettlementService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminSettlementController(AdminSettlementService adminSettlementService) {
        this.adminSettlementService = adminSettlementService;
    }

    /**
     * 관리자 정산 기능을 처리하는 POST /api/admin/settlements/{settlementId}/settle 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param settlementId 정산 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{settlementId}/settle")
    public ApiResponse<AdminSettlementResponse> settle(
        @AuthenticationPrincipal AdminPrincipal userDetails,
        @PathVariable Long settlementId
    ) {
        AdminSettlementResponse response = adminSettlementService.settle(userDetails, settlementId);
        return ApiResponse.success("정산을 실행했습니다.", response);
    }
}
