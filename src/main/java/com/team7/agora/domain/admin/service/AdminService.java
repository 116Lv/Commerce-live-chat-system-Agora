package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.admin.entity.Admin;
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
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public AdminMeResponse getMe(AdminPrincipal admin) {
        validateAdmin(admin);
        Admin entity = adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Admin account not found."));
        return AdminMeResponse.from(entity);
    }

    public AdminDashboardResponse getDashboard(AdminPrincipal admin) {
        validateAdmin(admin);
        return new AdminDashboardResponse(admin.getRole().name(), accessibleMenusFor(admin.getRole()));
    }

    private void validateAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private List<String> accessibleMenusFor(AdminRole role) {
        return switch (role) {
            case ROOT_ADMIN -> List.of("USERS", "USER_REPORTS", "PRODUCTS", "PRODUCT_REPORTS", "PAYMENTS", "REFUNDS", "SETTLEMENTS");
            case USER_ADMIN -> List.of("USERS", "USER_REPORTS");
            case PRODUCT_ADMIN -> List.of("PRODUCTS", "PRODUCT_REPORTS");
            case SETTLEMENT_ADMIN -> List.of("PAYMENTS", "REFUNDS", "SETTLEMENTS");
        };
    }
}
