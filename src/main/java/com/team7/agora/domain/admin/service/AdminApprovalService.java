package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminApprovalStatus;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminApprovalService {

    private final AdminApprovalRequestRepository approvalRequestRepository;
    private final AdminRepository adminRepository;

    public AdminApprovalService(
            AdminApprovalRequestRepository approvalRequestRepository,
            AdminRepository adminRepository
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.adminRepository = adminRepository;
    }

    public List<AdminApprovalRequestResponse> getRequests(AdminPrincipal admin, String status) {
        validateRootAdmin(admin);
        if (status == null || status.isBlank()) {
            return approvalRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(AdminApprovalRequestResponse::from)
                    .toList();
        }

        AdminApprovalStatus approvalStatus = parseStatus(status);
        return approvalRequestRepository.findAllByStatusOrderByCreatedAtDesc(approvalStatus).stream()
                .map(AdminApprovalRequestResponse::from)
                .toList();
    }

    public List<AdminApprovalRequestResponse> getMyRequests(AdminPrincipal admin) {
        Admin requester = getCurrentAdmin(admin);
        return approvalRequestRepository.findAllByRequesterOrderByCreatedAtDesc(requester).stream()
                .map(AdminApprovalRequestResponse::from)
                .toList();
    }

    @Transactional
    public AdminApprovalRequestResponse requestRoleChange(
            AdminPrincipal admin,
            Long targetAdminId,
            AdminRole requestedRole,
            String reason
    ) {
        Admin requester = getCurrentAdmin(admin);
        Admin target = adminRepository.findById(targetAdminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Target admin account not found."));
        if (target.getRole() == requestedRole) {
            throw new BusinessException(ErrorCode.CONFLICT, "Target admin already has the requested role.");
        }
        if (approvalRequestRepository.existsByRequesterAndTargetAdminAndRequestedRoleAndStatus(
                requester,
                target,
                requestedRole,
                AdminApprovalStatus.PENDING
        )) {
            throw new BusinessException(ErrorCode.CONFLICT, "A pending approval request already exists.");
        }

        AdminApprovalRequest request = AdminApprovalRequest.createRoleChange(requester, target, requestedRole, reason);
        try {
            return AdminApprovalRequestResponse.from(approvalRequestRepository.save(request));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "A pending approval request already exists.");
        }
    }

    @Transactional
    public AdminApprovalRequestResponse approve(AdminPrincipal admin, Long requestId, String memo) {
        Admin approver = getRootAdmin(admin);
        AdminApprovalRequest request = getRequest(requestId);
        validateRoleChange(admin, request.getTargetAdmin(), request.getRequestedRole());
        request.approve(approver, memo);
        return AdminApprovalRequestResponse.from(request);
    }

    @Transactional
    public AdminApprovalRequestResponse reject(AdminPrincipal admin, Long requestId, String memo) {
        Admin approver = getRootAdmin(admin);
        AdminApprovalRequest request = getRequest(requestId);
        request.reject(approver, memo);
        return AdminApprovalRequestResponse.from(request);
    }

    private AdminApprovalRequest getRequest(Long requestId) {
        return approvalRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Approval request not found."));
    }

    private Admin getCurrentAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account not found."));
    }

    private Admin getRootAdmin(AdminPrincipal admin) {
        validateRootAdmin(admin);
        return adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account not found."));
    }

    private void validateRootAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (admin.getRole() != AdminRole.ROOT_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only ROOT_ADMIN can approve requests.");
        }
    }

    private void validateRoleChange(AdminPrincipal admin, Admin target, AdminRole role) {
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

    private AdminApprovalStatus parseStatus(String status) {
        try {
            return AdminApprovalStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Invalid approval status.");
        }
    }
}
