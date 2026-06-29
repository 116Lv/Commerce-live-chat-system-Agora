// 관리자 계정 권한 변경 서비스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminAccountServiceTest {

    @Mock
    private AdminRepository adminRepository;

    private AdminAccountService createService() {
        return new AdminAccountService(adminRepository);
    }

    private AdminPrincipal principal(AdminRole role) {
        return new AdminPrincipal(99L, "root@admin.com", "encoded", role, AdminStatus.ACTIVE, "최고관리자");
    }

    @Test
    void changeRole_updatesAdminRoleWhenRootAdminRequestsAdminRole() {
        // given
        AdminAccountService service = createService();
        Admin admin = Admin.create("target@test.com", "encoded", "대상관리자", AdminRole.USER_ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        // when
        AdminUserResponse response = service.changeRole(principal(AdminRole.ROOT_ADMIN), 1L, AdminRole.PRODUCT_ADMIN);

        // then
        assertThat(response.role()).isEqualTo("PRODUCT_ADMIN");
        assertThat(admin.getRole()).isEqualTo(AdminRole.PRODUCT_ADMIN);
    }

    @Test
    void changeRole_throwsForbiddenWhenRequesterIsNotRootAdmin() {
        // given
        AdminAccountService service = createService();

        // when & then
        assertThatThrownBy(() -> service.changeRole(principal(AdminRole.USER_ADMIN), 1L, AdminRole.PRODUCT_ADMIN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void changeRole_throwsConflictWhenTargetAlreadyHasRole() {
        // given
        AdminAccountService service = createService();
        Admin admin = Admin.create("target@test.com", "encoded", "대상관리자", AdminRole.PRODUCT_ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        // when & then
        assertThatThrownBy(() -> service.changeRole(principal(AdminRole.ROOT_ADMIN), 1L, AdminRole.PRODUCT_ADMIN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
