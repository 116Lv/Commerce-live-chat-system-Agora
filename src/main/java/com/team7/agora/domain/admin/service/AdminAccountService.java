package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAccountService {

    private final AdminRepository adminRepository;

    public AdminAccountService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<AdminUserResponse> getAccounts(AdminPrincipal admin) {
        validateRootAdmin(admin);
        return adminRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Transactional
    public AdminUserResponse changeRole(AdminPrincipal admin, Long adminId, AdminRole role) {
        validateRootAdmin(admin);
        throw new BusinessException(ErrorCode.CONFLICT, "Admin role changes require an approval request.");
    }

    private void validateRootAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (admin.getRole() != AdminRole.ROOT_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only ROOT_ADMIN can manage admin accounts.");
        }
    }
}
