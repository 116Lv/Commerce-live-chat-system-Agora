package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        return new AdminDashboardResponse(admin.getRole().name(), accessibleMenusFor(admin.getPermissions()));
    }

    private void validateAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private List<String> accessibleMenusFor(Set<AdminPermission> permissions) {
        List<String> menus = new ArrayList<>();
        if (permissions.contains(AdminPermission.USER_MANAGE)) {
            menus.add("USERS");
        }
        if (permissions.contains(AdminPermission.REPORT_MANAGE)) {
            menus.add("USER_REPORTS");
            menus.add("PRODUCT_REPORTS");
        }
        if (permissions.contains(AdminPermission.PRODUCT_MANAGE)) {
            menus.add("PRODUCTS");
        }
        if (permissions.contains(AdminPermission.PAYMENT_MANAGE)) {
            menus.add("PAYMENTS");
            menus.add("REFUNDS");
            menus.add("SETTLEMENTS");
        }
        if (permissions.contains(AdminPermission.COUPON_MANAGE)) {
            menus.add("COUPONS");
        }
        return List.copyOf(menus);
    }
}
