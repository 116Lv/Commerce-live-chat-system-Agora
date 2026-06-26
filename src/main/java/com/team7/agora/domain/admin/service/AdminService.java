// 관리자 정보 조회와 대시보드 조회를 처리하는 서비스
package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 'getMe' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public AdminMeResponse getMe(CustomUserDetails admin) {
        validateAdmin(admin);
        User user = userRepository.findById(admin.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "관리자를 찾을 수 없습니다."));
        return AdminMeResponse.from(user);
    }

    /**
     * 'getDashboard' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public AdminDashboardResponse getDashboard(CustomUserDetails admin) {
        validateAdmin(admin);
        return new AdminDashboardResponse(admin.getRole().name(), accessibleMenusFor(admin.getRole()));
    }

    private void validateAdmin(CustomUserDetails admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!AdminRoleSupport.isAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 없는 계정입니다.");
        }
    }

    private List<String> accessibleMenusFor(UserRole role) {
        return switch (role) {
            case ROOT_ADMIN -> List.of("USERS", "USER_REPORTS", "PRODUCTS", "PRODUCT_REPORTS", "PAYMENTS", "REFUNDS", "SETTLEMENTS");
            case USER_ADMIN -> List.of("USERS", "USER_REPORTS");
            case PRODUCT_ADMIN -> List.of("PRODUCTS", "PRODUCT_REPORTS");
            case SETTLEMENT_ADMIN -> List.of("PAYMENTS", "REFUNDS", "SETTLEMENTS");
            default -> throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 없는 계정입니다.");
        };
    }
}
