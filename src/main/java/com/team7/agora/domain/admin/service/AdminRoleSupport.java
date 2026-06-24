// 관리자 역할 판별을 담당하는 지원 클래스
package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.user.enums.UserRole;

final class AdminRoleSupport {

    private AdminRoleSupport() {
    }

    static boolean isAdminRole(UserRole role) {
        return role == UserRole.ROOT_ADMIN
                || role == UserRole.USER_ADMIN
                || role == UserRole.PRODUCT_ADMIN
                || role == UserRole.SETTLEMENT_ADMIN;
    }
}
