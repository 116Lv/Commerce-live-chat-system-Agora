package com.team7.agora.global.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.enums.AdminRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class AdminPrincipalTest {

    @Test
    void authoritiesExposeRoleAndEffectivePermissions() {
        Admin admin = Admin.create("admin@test.com", "encoded", "admin", AdminRole.SETTLEMENT_ADMIN);
        admin.grantPermission(AdminPermission.REPORT_MANAGE);

        AdminPrincipal principal = AdminPrincipal.from(admin);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder(
                        "SETTLEMENT_ADMIN",
                        "PAYMENT_MANAGE",
                        "COUPON_MANAGE",
                        "REPORT_MANAGE"
                );
    }
}
