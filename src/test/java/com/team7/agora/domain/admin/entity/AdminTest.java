package com.team7.agora.domain.admin.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.enums.AdminRole;
import org.junit.jupiter.api.Test;

class AdminTest {

    @Test
    void effectivePermissionsIncludeRoleDefaultsAndExtraGrants() {
        Admin admin = Admin.create("payment@test.com", "encoded", "payment-admin", AdminRole.SETTLEMENT_ADMIN);

        admin.grantPermission(AdminPermission.REPORT_MANAGE);

        assertThat(admin.getEffectivePermissions())
                .containsExactlyInAnyOrder(
                        AdminPermission.PAYMENT_MANAGE,
                        AdminPermission.COUPON_MANAGE,
                        AdminPermission.REPORT_MANAGE
                );
    }
}
