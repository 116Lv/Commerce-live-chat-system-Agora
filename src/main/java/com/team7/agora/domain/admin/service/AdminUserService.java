package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param userRepository 입력 값
     */
    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @param pageable 입력 값
     * @return 처리 결과
     */
    public Page<AdminUserResponse> getUsers(CustomUserDetails admin, Pageable pageable) {
        validateUserAdmin(admin);
        return userRepository.findAll(pageable)
                .map(AdminUserResponse::from);
    }

    /**
     * 요청한 동작을 처리한다.
     * @param admin 입력 값
     * @param userId 입력 값
     * @param status 입력 값
     * @return 처리 결과
     */
    @Transactional
    public AdminUserResponse changeStatus(CustomUserDetails admin, Long userId, UserStatus status) {
        validateUserAdmin(admin);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        validateTargetStatusChange(admin, user);
        user.changeStatus(status);
        return AdminUserResponse.from(user);
    }

    private void validateUserAdmin(CustomUserDetails admin) {
        if (admin == null || !AdminRoleSupport.isUserAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "사용자 관리는 관리자만 수행할 수 있습니다.");
        }
    }

    private void validateTargetStatusChange(CustomUserDetails admin, User user) {
        if (AdminRoleSupport.isAdminRole(user.getRole()) && admin.getRole() != UserRole.ROOT_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 계정 상태 변경은 ROOT_ADMIN만 수행할 수 있습니다.");
        }
    }
}
