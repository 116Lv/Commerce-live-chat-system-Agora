package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
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
    private final AdminApprovalRequestRepository approvalRequestRepository;

    public AdminAccountService(
            AdminRepository adminRepository,
            AdminApprovalRequestRepository approvalRequestRepository
    ) {
        this.adminRepository = adminRepository;
        this.approvalRequestRepository = approvalRequestRepository;
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
        Admin target = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Target admin account not found."));

        validateRoleChange(admin, target, role);

        Admin requester = adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account not found."));
        AdminApprovalRequest history = AdminApprovalRequest.createRoleChange(
                requester,
                target,
                role,
                "ROOT_ADMIN immediate role change"
        );
        history.applyRoleChange();
        history.approve(requester, "ROOT_ADMIN immediate role change");
        approvalRequestRepository.save(history);

        return AdminUserResponse.from(target);
    }

    /**
     * ROOT_ADMIN을 강등시키는 변경일 때만 자기 자신 강등 금지, 마지막 1인 강등 금지를 검사한다.
     * findAllByRoleForUpdate로 ROOT_ADMIN 행들을 먼저 잠근 뒤 countByRole을 세는 이유는,
     * 두 강등 요청이 동시에 들어와도 둘 다 "2명 중 1명"으로 통과해 ROOT_ADMIN이 0명이
     * 되는 경쟁 상태를 막기 위함이다.
     */
    private void validateRoleChange(AdminPrincipal admin, Admin target, AdminRole role) {
        if (target.getRole() == role) {
            throw new BusinessException(ErrorCode.CONFLICT, "Target admin already has the requested role.");
        }
        if (target.getRole() == AdminRole.ROOT_ADMIN && role != AdminRole.ROOT_ADMIN) {
            if (target.getId().equals(admin.getAdminId())) {
                throw new BusinessException(ErrorCode.CONFLICT, "ROOT_ADMIN cannot demote their own account.");
            }

            adminRepository.findAllByRoleForUpdate(AdminRole.ROOT_ADMIN);
            if (adminRepository.countByRole(AdminRole.ROOT_ADMIN) <= 1) {
                throw new BusinessException(ErrorCode.CONFLICT, "At least one ROOT_ADMIN account must remain.");
            }
        }
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
