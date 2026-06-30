package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.global.auth.AdminPrincipal;

public final class AdminRoleSupport {

    private AdminRoleSupport() {
    }

    public static boolean hasPermission(AdminPrincipal admin, AdminPermission permission) {
        return admin != null && admin.getPermissions().contains(permission);
    }
}
