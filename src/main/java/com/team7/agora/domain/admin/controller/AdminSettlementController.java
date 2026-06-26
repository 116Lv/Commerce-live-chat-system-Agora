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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/settlements")
public class AdminSettlementController {

    private final AdminSettlementService adminSettlementService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param adminSettlementService 입력 값
     */
    public AdminSettlementController(AdminSettlementService adminSettlementService) {
        this.adminSettlementService = adminSettlementService;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param settlementId 입력 값
     * @return 처리 결과
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
