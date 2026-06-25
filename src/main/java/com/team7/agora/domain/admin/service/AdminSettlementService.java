package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminSettlementResponse;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminSettlementService {

    private final SettlementRepository settlementRepository;

    public AdminSettlementService(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    @Transactional
    public AdminSettlementResponse settle(CustomUserDetails admin, Long settlementId) {
        validateSettlementAdmin(admin);
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "정산 정보를 찾을 수 없습니다."));
        settlement.settle();
        return AdminSettlementResponse.from(settlement);
    }

    private void validateSettlementAdmin(CustomUserDetails admin) {
        if (admin == null || !AdminRoleSupport.isSettlementAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "정산 실행은 정산 관리자만 수행할 수 있습니다.");
        }
    }
}
