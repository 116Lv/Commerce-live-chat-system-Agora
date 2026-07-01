package com.team7.agora.domain.admin.enums;

import java.util.EnumSet;
import java.util.Set;

public enum AdminPermission {
    USER_MANAGE,
    REPORT_MANAGE,
    PRODUCT_MANAGE,
    PAYMENT_MANAGE,
    COUPON_MANAGE,
    ADMIN_ACCOUNT_MANAGE,
    APPROVAL_MANAGE;

    public static Set<AdminPermission> defaultsFor(AdminRole role) {
        return switch (role) {
            case ROOT_ADMIN -> EnumSet.allOf(AdminPermission.class);
            case USER_ADMIN -> EnumSet.of(USER_MANAGE, REPORT_MANAGE);
            case PRODUCT_ADMIN -> EnumSet.of(PRODUCT_MANAGE, REPORT_MANAGE);
            case SETTLEMENT_ADMIN -> EnumSet.of(PAYMENT_MANAGE, COUPON_MANAGE);
        };
    }
}
