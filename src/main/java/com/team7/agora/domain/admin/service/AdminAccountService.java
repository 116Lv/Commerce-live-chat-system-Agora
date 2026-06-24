// ROOT_ADMIN의 관리자 권한 변경을 처리하는 서비스
package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAccountService {

    private final UserRepository userRepository;

    public AdminAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AdminUserResponse changeRole(CustomUserDetails admin, Long userId, UserRole role) {
        validateRootAdmin(admin);
        validateTargetRole(role);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "계정을 찾을 수 없습니다."));
        user.changeRole(role);
        return AdminUserResponse.from(user);
    }

    private void validateRootAdmin(CustomUserDetails admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (admin.getRole() != UserRole.ROOT_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한 변경은 ROOT_ADMIN만 수행할 수 있습니다.");
        }
    }

    private void validateTargetRole(UserRole role) {
        if (!AdminRoleSupport.isAdminRole(role)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "관리자 역할로만 변경할 수 있습니다.");
        }
    }
}
