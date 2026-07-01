package com.team7.agora.domain.admin.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdminPermissionTest {

    @Test
    void defaultPermissionsMatchDomainAdminResponsibilities() {
        assertThat(AdminPermission.defaultsFor(AdminRole.USER_ADMIN))
                .containsExactlyInAnyOrder(AdminPermission.USER_MANAGE, AdminPermission.REPORT_MANAGE);
        assertThat(AdminPermission.defaultsFor(AdminRole.PRODUCT_ADMIN))
                .containsExactlyInAnyOrder(AdminPermission.PRODUCT_MANAGE, AdminPermission.REPORT_MANAGE);
        assertThat(AdminPermission.defaultsFor(AdminRole.SETTLEMENT_ADMIN))
                .containsExactlyInAnyOrder(AdminPermission.PAYMENT_MANAGE, AdminPermission.COUPON_MANAGE);
        assertThat(AdminPermission.defaultsFor(AdminRole.ROOT_ADMIN))
                .containsExactlyInAnyOrder(AdminPermission.values());
    }
}
