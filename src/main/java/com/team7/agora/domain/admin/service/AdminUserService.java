package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AdminUserResponse> getUsers(CustomUserDetails admin, Pageable pageable) {
        validateUserAdmin(admin);
        return userRepository.findAll(pageable).stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    private void validateUserAdmin(CustomUserDetails admin) {
        if (admin == null || !AdminRoleSupport.isUserAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "사용자 관리는 관리자만 수행할 수 있습니다.");
        }
    }
}
