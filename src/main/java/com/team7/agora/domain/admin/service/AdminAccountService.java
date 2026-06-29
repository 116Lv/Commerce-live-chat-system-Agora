package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAccountService {

    private final AdminRepository adminRepository;

    public AdminAccountService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Transactional
    public AdminUserResponse changeRole(AdminPrincipal admin, Long adminId, AdminRole role) {
        validateRootAdmin(admin);
        Admin target = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Admin account not found."));
        target.changeRole(role);
        return AdminUserResponse.from(target);
    }

    private void validateRootAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (admin.getRole() != AdminRole.ROOT_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only ROOT_ADMIN can change admin roles.");
        }
    }
}
