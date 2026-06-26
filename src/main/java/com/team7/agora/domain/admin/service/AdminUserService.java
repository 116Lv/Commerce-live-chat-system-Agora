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
 * Application service that coordinates admin user use cases.
 */
@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * Creates a admin user service instance.
     * @param userRepository the user repository value
     */
    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns users data.
     * @param admin the admin value
     * @param pageable the pageable value
     * @return the get users result
     */
    public Page<AdminUserResponse> getUsers(CustomUserDetails admin, Pageable pageable) {
        validateUserAdmin(admin);
        return userRepository.findAll(pageable)
                .map(AdminUserResponse::from);
    }

    /**
     * Handles change status behavior.
     * @param admin the admin value
     * @param userId the user id value
     * @param status the status value
     * @return the change status result
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
