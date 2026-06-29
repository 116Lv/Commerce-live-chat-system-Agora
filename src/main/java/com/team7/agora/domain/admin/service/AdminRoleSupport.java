package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.enums.AdminRole;

public final class AdminRoleSupport {

    private AdminRoleSupport() {
    }

    public static boolean isSettlementAdminRole(AdminRole role) {
        return role == AdminRole.ROOT_ADMIN || role == AdminRole.SETTLEMENT_ADMIN;
    }

    public static boolean isUserAdminRole(AdminRole role) {
        return role == AdminRole.ROOT_ADMIN || role == AdminRole.USER_ADMIN;
    }

    public static boolean isProductAdminRole(AdminRole role) {
        return role == AdminRole.ROOT_ADMIN || role == AdminRole.PRODUCT_ADMIN;
    }
}
