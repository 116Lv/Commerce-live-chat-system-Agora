// 관리자 정보와 대시보드 서비스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    private AdminService createService() {
        return new AdminService(adminRepository);
    }

    private AdminPrincipal principal(AdminRole role) {
        return new AdminPrincipal(1L, "admin@test.com", "encoded", role, AdminStatus.ACTIVE, "관리자");
    }

    @Test
    void getMe_returnsAdminAccountInfo() {
        // given
        AdminService service = createService();
        Admin admin = Admin.create("admin@test.com", "encoded", "관리자", AdminRole.ROOT_ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        // when
        AdminMeResponse response = service.getMe(principal(AdminRole.ROOT_ADMIN));

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("admin@test.com");
        assertThat(response.role()).isEqualTo("ROOT_ADMIN");
    }

    @Test
    void getDashboard_returnsMenusScopedToUserAdmin() {
        // given
        AdminService service = createService();

        // when
        AdminDashboardResponse response = service.getDashboard(principal(AdminRole.USER_ADMIN));

        // then
        assertThat(response.role()).isEqualTo("USER_ADMIN");
        assertThat(response.accessibleMenus()).containsExactly("USERS", "USER_REPORTS", "PRODUCT_REPORTS");
    }

    @Test
    void getDashboard_returnsMenusFromGrantedPermissions() {
        AdminService service = createService();
        AdminPrincipal admin = new AdminPrincipal(
                1L,
                "admin@test.com",
                "encoded",
                AdminRole.SETTLEMENT_ADMIN,
                AdminStatus.ACTIVE,
                "관리자",
                Set.of(AdminPermission.PAYMENT_MANAGE, AdminPermission.COUPON_MANAGE, AdminPermission.REPORT_MANAGE)
        );

        AdminDashboardResponse response = service.getDashboard(admin);

        assertThat(response.accessibleMenus())
                .contains("PAYMENTS", "REFUNDS", "SETTLEMENTS", "COUPONS", "USER_REPORTS", "PRODUCT_REPORTS");
    }

    @Test
    void getDashboard_throwsUnauthorizedWithoutPrincipal() {
        // given
        AdminService service = createService();

        // when & then
        assertThatThrownBy(() -> service.getDashboard(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
