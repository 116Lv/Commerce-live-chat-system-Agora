package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminSettlementResponse;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 정산 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminSettlementService {

    private final SettlementRepository settlementRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param settlementRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public AdminSettlementService(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    /**
     * 'settle' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param admin 인증된 관리자 정보
     * @param settlementId 정산 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public AdminSettlementResponse settle(AdminPrincipal admin, Long settlementId) {
        validateSettlementAdmin(admin);
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "정산 정보를 찾을 수 없습니다."));
        settlement.settle();
        return AdminSettlementResponse.from(settlement);
    }

    private void validateSettlementAdmin(AdminPrincipal admin) {
        if (!AdminRoleSupport.hasPermission(admin, AdminPermission.PAYMENT_MANAGE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "정산 실행은 정산 관리자만 수행할 수 있습니다.");
        }
    }
}
